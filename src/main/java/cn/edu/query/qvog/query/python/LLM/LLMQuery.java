package cn.edu.query.qvog.query.python.LLM;

import cn.edu.engine.qvog.engine.dsl.fluent.query.CompleteQuery;
import cn.edu.engine.qvog.engine.dsl.fluent.query.QueryDescriptor;
import cn.edu.engine.qvog.engine.dsl.lib.engine.QueryEngine;
import cn.edu.engine.qvog.engine.dsl.lib.flow.TaintFlowPredicate;
import cn.edu.engine.qvog.engine.helper.JsonHelper;
import cn.edu.engine.qvog.engine.language.python.PythonQuery;
import cn.edu.engine.qvog.engine.language.python.lib.predicate.LLMMatch;
import cn.edu.engine.qvog.engine.language.python.lib.predicate.LineNumberMatch;
import cn.edu.engine.qvog.engine.ml.Location;
import cn.edu.engine.qvog.engine.ml.PredictTypes;
import cn.edu.query.qvog.query.python.CWE_022.TaintedPath;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionAudioParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.List;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class LLMQuery extends PythonQuery {
    public static void main(String[] args) {
        QueryEngine.getInstance().execute(new TaintedPath()).close();
    }


    @Override
    public String getQueryName() {
        return "LLM: LLM Query";
    }

    public static String readJsonFromResources(String fileName) {
        StringBuilder jsonString = new StringBuilder();
        try {
            InputStream inputStream = LLMQuery.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream == null) {
                System.out.println("Sorry, unable to find " + fileName);
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString.toString();
    }

    private static void callPythonScript(String path, String cwe, ArrayList<String> api_list) throws IOException, InterruptedException {
        // 获取资源输入流
        InputStream inputStream = LLMQuery.class.getClassLoader().getResourceAsStream("python/script.py");
        if (inputStream == null) {
            System.out.println("Python script not found!");
            return;
        }

        // 创建临时文件
        Path tempFile = Files.createTempFile("temp", ".py");
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

        // 执行Python脚本
        ProcessBuilder pb = new ProcessBuilder("python", tempFile.toString(), path, cwe);
        Process process = pb.start();

        // 处理输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            api_list.add(line.replace("\\n", "\n"));
//            System.out.println(line);
        }

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待进程结束并获取退出码
        int exitCode = process.waitFor();
        System.out.println("Python script exited with code: " + exitCode);

        // 删除临时文件
        Files.deleteIfExists(tempFile);
    }

    private static String generateSystemPrompt(JSONObject cweMap, String cwe) {
        JSONObject map = JsonHelper.getObject(cweMap, cwe);
        return String.format("""
You are a security expert. You will be provided with a list of APIs,
and you need to identify potential taint sources, barrier or sinks.
%s

Return the result as a json list with each object in the format:

{ "file": <file name>,
"function_name": <function name>,
"line_number_range": (<starting line number>, <ending line number>),
"line_number": <line number,
"line_content": <line content>,
"type": <"source", "barrier" or "sink"> }

DO NOT OUTPUT ANYTHING OTHER THAN JSON.""", JsonHelper.getValue(map, "system_prompt"));
    }

    private static String generateUserPrompt(JSONObject cweMap, String cwe, String content) {
        JSONObject map = JsonHelper.getObject(cweMap, cwe);
        return String.format("""
%s
    
Some example source/sink methods are:
%s

Among the following functions,
assuming that the arguments passed to the given lines is malicious, 
what are the functions that are potential source or sink to %s attack (%s)?
\
File_name,Function_name,Line_number_range,Function_contents_with_line_numbers
%s""", JsonHelper.getValue(map, "description"), JsonHelper.getValue(map, "example"),
                JsonHelper.getValue(map, "title"), cwe, content);
    }

    private static String sendRequest(String systemPrompt, String userPrompt, JSONObject api) {
        String apiKey = JsonHelper.getValue(api, "apiKey");
        String baseUrl = JsonHelper.getValue(api, "baseUrl");
        String model = JsonHelper.getValue(api, "model");
        try {
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .build();

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(model)
                    .addSystemMessage(systemPrompt)
                    .addUserMessage(userPrompt)
                    .temperature(0)
                    .build();
            ChatCompletion response = client.chat().completions().create(params);
            return response.choices().get(0).message().content().get();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    private static int checkLineNumber(String path, JSONObject obj) {
        String file = JsonHelper.getValue(obj, "file");
        String lineContent = JsonHelper.getValue(obj, "line_content");
        int lineno = JsonHelper.getIntValue(obj, "line_number");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path, file)));
            String line;
            int currentLine = 0;

            while ((line = reader.readLine()) != null) {
                currentLine++;
                if (currentLine >= lineno - 3 && currentLine <= lineno + 3) {
                    if (line.contains(lineContent)) {
                        return currentLine + 1;
                    }
                }
                if (currentLine > lineno + 3) break;
            }
        } catch (IOException e) {
            System.out.println("Error: " + JsonHelper.getValue(obj, "file"));
            throw new RuntimeException(e);
        }
        return lineno;
    }

    private static ArrayList<Location> llmApi(String path, String cwe, JSONObject api) {
        ArrayList<String> api_list = new ArrayList<>();
        ArrayList<Location> llmResult = new ArrayList<>();
        int step = 10;

        try {
            JSONObject cweMap = (JSONObject) new JSONParser().parse(readJsonFromResources("json/cweMap.json"));
            callPythonScript(path, cwe, api_list);
            System.out.println(api_list.size());
            for (int i = 0; i < api_list.size(); i += step) {
                String content = String.join("\n", api_list.subList(i, Math.min(i + step, api_list.size())));
                String systemPrompt = generateSystemPrompt(cweMap, cwe);
                String userPrompt = generateUserPrompt(cweMap, cwe, content);
                String response = sendRequest(systemPrompt, userPrompt, api);
//                System.out.println(response);
                JSONArray responseJson = (JSONArray) new JSONParser().parse(response
                        .replace("```json", "").replace("```", "")
                        .replace("(", "[").replace(")", "]"));
                for (Object o : responseJson) {
                    JSONObject obj = (JSONObject) o;
                    int lineno = checkLineNumber(path, obj);
                    String file = JsonHelper.getValue(obj, "file").replace("/", "\\");
                    PredictTypes type = PredictTypes.fromString(JsonHelper.getValue(obj, "type"));
                    llmResult.add(new Location(file, lineno, type));
                }
            }
        } catch (Throwable e) {
            System.err.println("Error occurred while walking through files: " + e.getMessage());
        }
        JSONObject totalMap = new JSONObject();
        JSONArray pred_source = new JSONArray();
        JSONArray pred_sink = new JSONArray();
        JSONArray pred_barrier = new JSONArray();
        for (Location result : llmResult) {
            if (result.getType() == PredictTypes.Source) {
                pred_source.add(result.getFile() + ":" + result.getLineno());
            } else if (result.getType() == PredictTypes.Sink) {
                pred_sink.add(result.getFile() + ":" + result.getLineno());
            } else if (result.getType() == PredictTypes.Barrier) {
                pred_barrier.add(result.getFile() + ":" + result.getLineno());
            }
        }
        totalMap.put("pred_source", pred_source);
        totalMap.put("pred_sink", pred_sink);
        totalMap.put("pred_barrier", pred_barrier);
        System.out.println(JsonHelper.dumps(totalMap));
        return llmResult;
    }


    @Override
    public CompleteQuery run() {
        try {
            JSONObject config = JsonHelper.load("./config.json");
            JSONObject llm = JsonHelper.tryGetObject(config, "llm");

            String path = JsonHelper.tryGetValue(llm, "path");
            String cwe = JsonHelper.tryGetValue(llm, "cwe");
            JSONObject api = JsonHelper.tryGetObject(llm, "api");
            ArrayList<Location> llmResult = llmApi(path, cwe.toLowerCase(), api);
            return QueryDescriptor.open()
                    .from("source", new LLMMatch(llmResult, PredictTypes.Source))
                    .from("sink", new LLMMatch(llmResult, PredictTypes.Sink))
                    .from("barrier", new LLMMatch(llmResult, PredictTypes.Barrier))
                    .where(TaintFlowPredicate.with()
                            .source("source")
                            .sink("sink")
                            .barrier("barrier")
                            .as("path").exists())
                    .select("source", "sink", "path");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


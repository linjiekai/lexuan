package com.zhuanbo.core.util;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class JacksonUtil {
    private static ObjectMapper formatObjectMapper;

    //单例
    public static ObjectMapper getInstance() {
        if (formatObjectMapper == null) {
            synchronized (JacksonUtil.class) {
                if (formatObjectMapper == null) {
                    formatObjectMapper = (ObjectMapper) SpringContextUtil.getBean(ObjectMapper.class);
                }
            }
        }
        return formatObjectMapper;
    }

    public static String parseString(String body, String field) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null)
                return leaf.asText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer parseInteger(String body, String field) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null)
                return leaf.asInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Integer> parseIntegerList(String body, String field) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(body);
            JsonNode leaf = node.get(field);

            if (leaf != null)
                return mapper.convertValue(leaf, new TypeReference<List<String>>() {
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Boolean parseBoolean(String body, String field) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null)
                return leaf.asBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Short parseShort(String body, String field) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null) {
                Integer value = leaf.asInt();
                return value.shortValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Byte parseByte(String body, String field) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(body);
            JsonNode leaf = node.get(field);
            if (leaf != null) {
                Integer value = leaf.asInt();
                return value.byteValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T parseObject(String body, String field, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(body);
            node = node.get(field);
            return mapper.treeToValue(node, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object toNode(String json) {
        if (json == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(json);
            return jsonNode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * javaBean,list,array ==》json string
     * 转String 效率：ObjectMapper>fastjson
     */
    public static String objTojson(Object obj)  {
        try {
            return getInstance().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * json string==> avaBean
     */
    public static <T> T jsonToObj(String jsonStr, Class<T> clazz) {
        try {
            return getInstance().readValue(jsonStr, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * json string convert to map
     */
    public static <T> Map<String, Object> jsonToMap(String jsonStr) {
        try {
            return getInstance().readValue(jsonStr, Map.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * json array string convert to list with javaBean
     * 转List效率：fastjson>ObjectMapper
     */
    public static <T> List<T> jsonTolist(String jsonArrayStr, Class<T> clazz) {
        try {
            return JSONArray.parseArray(jsonArrayStr, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * map convert to javaBean
     */
    public static <T> T map2pojo(Map map, Class<T> clazz) {
        return getInstance().convertValue(map, clazz);
    }


}

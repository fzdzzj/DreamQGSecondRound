package com.qg.server.ai.util;

public class LocationNormalizeUtil {
    private LocationNormalizeUtil() {
    }

    public static String normalize(String location){
        if(location==null||location.isBlank()){
            return "未知";
        }
        String text=location.trim()
                .replace(" ","")
                .replace("  ","");


        // 西区一栋
        if (text.contains("西一") || text.contains("西1") || text.contains("西区1") || text.contains("西一区1")) {
            return "西区一栋";
        }

        // 西区二栋
        if (text.contains("西二") || text.contains("西2") || text.contains("西区2") || text.contains("西二区2")) {
            return "西区二栋";
        }

        // 图书馆
        if (text.contains("图书馆")) {
            return "图书馆";
        }

        // 食堂
        if (text.contains("食堂") || text.contains("饭堂")) {
            if (text.contains("西")) {
                return "西区食堂";
            }
            if (text.contains("东")) {
                return "东区食堂";
            }
            return "食堂";
        }

        // 教学楼
        if (text.contains("教学楼") || text.matches(".*[A-Z]教.*")) {
            return "教学楼";
        }

        return location.trim();
    }
}
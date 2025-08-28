package com.light.translate.communicate.utils;

import java.util.*;

public class ListeningQuestionGenerator {

    // 题型枚举
    public enum QuestionType {
        WORD, DETAIL, ORDER
    }

    // 题目数据结构
    public static class ListeningQuestion {
        public QuestionType type;
        public String question;
        public List<String> options;
        public String answer;

        @Override
        public String toString() {
            return "Type: " + type + "\n" +
                    "Question: " + question + "\n" +
                    "Options: " + options + "\n" +
                    "Answer: " + answer + "\n";
        }
    }

    /**
     * 生成单词听辨题
     */
    public static ListeningQuestion generateWordQuestion(String sentence) {
        String[] words = sentence.replaceAll("[^a-zA-Z ]", "").split(" ");
        Random random = new Random();
        String target = words[random.nextInt(words.length)];

        // 干扰项：简单做法，造几个相似词
        List<String> options = new ArrayList<>();
        options.add(target);
        options.add(target + "s");
        if (target.length() > 3) {
            options.add(target.substring(0, target.length() - 1));
        } else {
            options.add(target + "y");
        }
        Collections.shuffle(options);

        ListeningQuestion q = new ListeningQuestion();
        q.type = QuestionType.WORD;
        q.question = "听音频，选择你听到的单词：";
        q.options = options;
        q.answer = target;
        return q;
    }

    /**
     * 生成句子细节题
     */
    public static ListeningQuestion generateDetailQuestion(String sentence) {
        String original = sentence.trim();

        List<String> options = new ArrayList<>();
        options.add(original);

        // 简单规则修改：替换一个数字 / 动词 / 名词
        if (original.matches(".*\\d+.*")) {
            options.add(original.replaceAll("\\d+", "11"));
        } else if (original.contains("is")) {
            options.add(original.replace("is", "was"));
        } else if (original.contains("are")) {
            options.add(original.replace("are", "were"));
        } else {
            options.add(original.replaceFirst("\\b\\w+\\b", "Tom"));
        }

        options.add(original + " today");
        Collections.shuffle(options);

        ListeningQuestion q = new ListeningQuestion();
        q.type = QuestionType.DETAIL;
        q.question = "听音频，选择正确的句子：";
        q.options = options;
        q.answer = original;
        return q;
    }

    /**
     * 生成顺序题
     */
    public static ListeningQuestion generateOrderQuestion(String sentence) {
        // 保留字母、空格和撇号
        String clean = sentence.replaceAll("[^a-zA-Z' ]", "").trim();
        // 拆分单词
        List<String> words = new ArrayList<>(Arrays.asList(clean.split("\\s+")));

        // 正确答案
        String correct = String.join(" ", words);

        // 打乱顺序
        Collections.shuffle(words);

        ListeningQuestion q = new ListeningQuestion();
        q.type = QuestionType.ORDER;
        q.question = "听音频，选择单词排序：";
        q.options = words;
        q.answer = correct;
        return q;
    }

    // 测试
    public static void main(String[] args) {
        String sentence = "Don’t look at my cards - that’s cheating .";

        ListeningQuestion q1 = generateWordQuestion(sentence);
        ListeningQuestion q2 = generateDetailQuestion(sentence);
        ListeningQuestion q3 = generateOrderQuestion(sentence);

        System.out.println(q1);
        System.out.println(q2);
        System.out.println(q3);
    }
}

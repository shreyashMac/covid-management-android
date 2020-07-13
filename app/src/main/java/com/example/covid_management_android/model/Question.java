package com.example.covid_management_android.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Question {

    private Integer id;
    private String question;
    private String createdAt;
    private String updatedAt;
    private List<QAnswerOption> qAnswerOptions = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<QAnswerOption> getQAnswerOptions() {
        return qAnswerOptions;
    }

    public void setQAnswerOptions(List<QAnswerOption> qAnswerOptions) {
        this.qAnswerOptions = qAnswerOptions;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
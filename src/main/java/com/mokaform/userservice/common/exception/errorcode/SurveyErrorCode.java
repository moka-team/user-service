package com.mokaform.userservice.common.exception.errorcode;

import org.springframework.http.HttpStatus;

public enum SurveyErrorCode implements ErrorCode {

    INVALID_SORT_TYPE("S001", HttpStatus.BAD_REQUEST, "Invalid survey sort type"),
    NO_PERMISSION_TO_DELETE_SURVEY("S002", HttpStatus.NOT_FOUND, "해당 설문을 삭제할 수 없습니다."),
    SURVEY_NOT_FOUND("S003", HttpStatus.BAD_REQUEST, "존재하지 않는 설문입니다."),
    NO_PERMISSION_TO_GET_SURVEY_INFO("S004", HttpStatus.NOT_FOUND, "설문 정보를 얻을 수 없습니다."),
    NO_PERMISSION_TO_UPDATE_SURVEY("S005", HttpStatus.NOT_FOUND, "설문을 수정할 수 없습니다."),
    INVALID_START_DATE("S006", HttpStatus.BAD_REQUEST, "설문 시작일은 오늘 이전일 수 없습니다."),
    INVALID_END_DATE("S007", HttpStatus.BAD_REQUEST, "설문 종료일은 시작일 이전일 수 없습니다."),
    INVALID_CATEGORY_TYPE("S008", HttpStatus.BAD_REQUEST, "잘못된 카테고리 종류입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    SurveyErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}

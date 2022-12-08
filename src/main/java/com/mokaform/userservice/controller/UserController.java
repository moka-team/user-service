package com.mokaform.userservice.controller;

import com.mokaform.userservice.common.jwt.JwtAuthentication;
import com.mokaform.userservice.common.jwt.JwtService;
import com.mokaform.userservice.common.response.ApiResponse;
import com.mokaform.userservice.common.util.constant.EmailType;
import com.mokaform.userservice.dto.request.LocalLoginRequest;
import com.mokaform.userservice.dto.request.ResetPasswordRequest;
import com.mokaform.userservice.dto.request.SignupRequest;
import com.mokaform.userservice.dto.response.DuplicateValidationResponse;
import com.mokaform.userservice.dto.response.LocalLoginResponse;
import com.mokaform.userservice.dto.response.UserGetResponse;
import com.mokaform.userservice.service.EmailService;
import com.mokaform.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Tag(name = "유저", description = "유저 관련 API입니다.")
@RestController
@RequestMapping("/user-service")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final EmailService emailService;

    private Environment env;

    public UserController(UserService userService,
                          JwtService jwtService,
                          EmailService emailService,
                          Environment env) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.env = env;
    }

    @GetMapping("/health_check")
    public String status(){
        return String.format("It's working in MOKA User Service"
                + ", port(local.server.port) = " + env.getProperty("local.server.port")
                + ", port(server.port) = " + env.getProperty("server.port"));
    }

    @Operation(summary = "회원가입", description = "회원가입 API입니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signUp(@RequestBody @Valid SignupRequest request) {
        userService.createUser(request);

        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .message("새로운 유저 생성이 성공하였습니다.")
                        .build());
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일 중복 체크하는 API입니다.")
    @GetMapping("/check-email-duplication")
    public ResponseEntity<ApiResponse<DuplicateValidationResponse>> checkEmailDuplication(@RequestParam(value = "email") String email) {
        DuplicateValidationResponse response = userService.checkEmailDuplication(email);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("이메일 중복 확인 성공하였습니다.")
                .data(response)
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 체크하는 API입니다.")
    @GetMapping("/check-nickname-duplication")
    public ResponseEntity<ApiResponse<DuplicateValidationResponse>> checkNicknameDuplication(@RequestParam(value = "nickname") String nickname) {
        DuplicateValidationResponse response = userService.checkNicknameDuplication(nickname);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("닉네임 중복 확인 성공하였습니다.")
                .data(response)
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @Operation(summary = "로그인", description = "로그인하는 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    headers = @Header(
                            name = "Set-Cookie",
                            schema = @Schema(
                                    type = "String",
                                    example = "refreshToken=${REFRESH_TOKEN}; Max-Age=1209599; Expires=Tue, 22-Nov-2022 09:07:55 GMT; Path=/; Secure; HttpOnly")))
    })
    @PostMapping(path = "/login")
    public ResponseEntity<ApiResponse<LocalLoginResponse>> login(@RequestBody @Valid LocalLoginRequest request,
                                                                 HttpServletResponse response) {
        jwtService.login(request, response);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("로그인 성공하였습니다.")
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @Operation(summary = "나의 정보 조회", description = "나의 정보 조회하는 API입니다.")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<UserGetResponse>> getUser(@Parameter(hidden = true) @AuthenticationPrincipal JwtAuthentication authentication) {
        UserGetResponse response = userService.getUserInfo(authentication.email);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("나의 정보 조회가 성공하였습니다.")
                .data(response)
                .build();

        return ResponseEntity.ok()
                .body(apiResponse);
    }

    @Operation(summary = "로그아웃", description = "로그아웃하는 API입니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@Parameter(hidden = true) @AuthenticationPrincipal JwtAuthentication authentication) {
        jwtService.logout(authentication.accessToken);
        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .message("로그아웃을 성공하였습니다.")
                        .build());
    }

    @Operation(summary = "토큰 재발급", description = "access token을 재발급하는 API입니다.")
    @PostMapping("/token/reissue")
    public ResponseEntity<ApiResponse> reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        jwtService.reissueAccessToken(request, response);

        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .message("토큰이 재발급되었습니다.")
                        .build());
    }

    @Operation(summary = "회원가입 - 이메일 검증 - 전송", description = "회원가입할 때, 이메일 검증을 위해 이메일을 전송하는 API입니다.")
    @PostMapping("/signup/email-verification/send")
    public ResponseEntity<ApiResponse> sendSignUpVerificationEmail(@RequestParam(value = "email") String email) {
        emailService.sendVerificationCode(EmailType.SIGN_IN, email);

        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .message("인증번호가 포함된 이메일 전송이 완료되었습니다.")
                        .build());
    }

    @Operation(summary = "회원가입 - 이메일 검증 - 인증번호 확인", description = "회원가입할 때, 인증번호를 확인하는 API입니다.")
    @GetMapping("/signup/email-verification/check")
    public ResponseEntity<ApiResponse> checkSignUpVerificationEmail(@RequestParam(value = "email") String email,
                                                                    @RequestParam(value = "code") String code) {
        emailService.checkVerificationCode(EmailType.SIGN_IN, email, code);

        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .message("인증번호 확인이 완료되었습니다.")
                        .build());
    }

    @Operation(summary = "비밀번호 재설정 - 이메일 검증 - 전송", description = "비밀번호 재설정할 때, 이메일 검증을 위해 이메일을 전송하는 API입니다.")
    @PostMapping("/reset-password/email-verification/send")
    public ResponseEntity<ApiResponse> sendResetPasswordVerificationEmail(@RequestParam(value = "email") String email) {
        emailService.sendVerificationCode(EmailType.RESET_PASSWORD, email);

        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .message("인증번호가 포함된 이메일 전송이 완료되었습니다.")
                        .build());
    }

    @Operation(summary = "비밀번호 재설정 - 이메일 검증 - 인증번호 확인", description = "비밀번호 재설정할 때, 인증번호를 확인하는 API입니다.")
    @GetMapping("/reset-password/email-verification/check")
    public ResponseEntity<ApiResponse> checkResetPasswordVerificationEmail(@RequestParam(value = "email") String email,
                                                                           @RequestParam(value = "code") String code) {
        emailService.checkVerificationCode(EmailType.RESET_PASSWORD, email, code);

        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .message("인증번호 확인이 완료되었습니다.")
                        .build());
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀전호를 재설정하는 API입니다.")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        userService.updatePassword(request);

        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .message("비밀번호 재설정이 완료되었습니다.")
                        .build());
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴하는 API입니다.")
    @PostMapping("/withdrawal")
    public ResponseEntity<ApiResponse> withdraw(@Parameter(hidden = true) @AuthenticationPrincipal JwtAuthentication authentication) {
        userService.withdraw(authentication.email);

        return ResponseEntity.ok()
                .body(ApiResponse.builder()
                        .message("회원 탈퇴가 완료되었습니다.")
                        .build());
    }
}

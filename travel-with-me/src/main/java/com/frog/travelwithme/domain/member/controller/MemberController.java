package com.frog.travelwithme.domain.member.controller;

import com.frog.travelwithme.domain.member.controller.dto.MemberDto;
import com.frog.travelwithme.domain.member.controller.dto.MemberDto.EmailVerificationResult;
import com.frog.travelwithme.domain.member.service.MemberService;
import com.frog.travelwithme.global.dto.SingleResponseDto;
import com.frog.travelwithme.global.security.auth.userdetails.CustomUserDetails;
import com.frog.travelwithme.global.validation.CustomAnnotationCollection.CustomEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static com.frog.travelwithme.global.enums.EnumCollection.ResponseBody.SUCCESS_MEMBER_FOLLOW;
import static com.frog.travelwithme.global.enums.EnumCollection.ResponseBody.SUCCESS_MEMBER_UNFOLLOW;

/**
 * 작성자: 김찬빈
 * 버전 정보: 1.0.0
 * 작성일자: 2023/03/29
 **/
@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity signUp(@Valid @RequestBody MemberDto.SignUp signUpDto) {
        MemberDto.Response response = memberService.signUp(signUpDto);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }

    @GetMapping("/{email}")
    public ResponseEntity getMember(@PathVariable("email") @Valid @CustomEmail String email) {
        MemberDto.Response response = memberService.findMemberByEmail(email);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @PatchMapping
    public ResponseEntity patchMember(@AuthenticationPrincipal CustomUserDetails user,
                                      @RequestBody MemberDto.Patch patchDto) {
        String email = user.getEmail();
        MemberDto.Response response = memberService.updateMember(patchDto, email);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @PatchMapping("/images")
    public ResponseEntity patchProfileImage(@AuthenticationPrincipal CustomUserDetails user,
                                            @RequestPart("file") MultipartFile multipartFile) {
        String email = user.getEmail();
        MemberDto.Response response = memberService.changeProfileImage(multipartFile, email);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @DeleteMapping("/images")
    public ResponseEntity deleteProfileImage(@AuthenticationPrincipal CustomUserDetails user) {
        String email = user.getEmail();
        MemberDto.Response response = memberService.removeProfileImage(email);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity deleteMember(@AuthenticationPrincipal CustomUserDetails user) {
        String email = user.getEmail();
        memberService.deleteMember(email);

        return new ResponseEntity<>(new SingleResponseDto<>("Member deleted successfully"), HttpStatus.NO_CONTENT);
    }

    @PostMapping("/emails/verification-requests")
    public ResponseEntity sendMessage(@RequestParam("email") @Valid @CustomEmail String email) {
        memberService.sendCodeToEmail(email);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/emails/verifications")
    public ResponseEntity verificationEmail(@RequestParam("email") @Valid @CustomEmail String email,
                                            @RequestParam("code") String authCode) {
        EmailVerificationResult response = memberService.verifiedCode(email, authCode);

        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @PostMapping("/follow/{followee-email}")
    public ResponseEntity follow(@PathVariable("followee-email") String followeeEmail,
                                 @AuthenticationPrincipal CustomUserDetails user) {
        String followerEmail = user.getEmail();
        memberService.follow(followerEmail, followeeEmail);

        return new ResponseEntity<>(new SingleResponseDto<>(SUCCESS_MEMBER_FOLLOW.getDescription()), HttpStatus.OK);
    }

    @DeleteMapping("/unfollow/{followee-email}")
    public ResponseEntity unfollow(@PathVariable("followee-email") String followeeEmail,
                                   @AuthenticationPrincipal CustomUserDetails user) {
        String followerEmail = user.getEmail();
        memberService.unfollow(followerEmail, followeeEmail);

        return new ResponseEntity<>(new SingleResponseDto<>(SUCCESS_MEMBER_UNFOLLOW.getDescription()), HttpStatus.OK);
    }

    @PostMapping("/check-duplicated-emails")
    public ResponseEntity checkDuplicatedEmail(@RequestParam("email") String email) {
        memberService.checkDuplicatedEmail(email);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/check-duplicated-nicknames")
    public ResponseEntity checkDuplicatedNickname(@RequestParam("nickname") String nickname) {
        memberService.checkDuplicatedNickname(nickname);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}

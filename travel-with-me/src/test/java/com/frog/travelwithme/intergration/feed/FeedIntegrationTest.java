package com.frog.travelwithme.intergration.feed;

import com.frog.travelwithme.domain.feed.controller.dto.FeedDto;
import com.frog.travelwithme.domain.feed.entity.Tag;
import com.frog.travelwithme.domain.feed.repository.TagRepository;
import com.frog.travelwithme.domain.feed.service.FeedService;
import com.frog.travelwithme.domain.feed.service.TagService;
import com.frog.travelwithme.domain.member.controller.dto.MemberDto;
import com.frog.travelwithme.domain.member.service.MemberService;
import com.frog.travelwithme.global.config.AES128Config;
import com.frog.travelwithme.global.security.auth.controller.dto.TokenDto;
import com.frog.travelwithme.global.security.auth.jwt.JwtTokenProvider;
import com.frog.travelwithme.global.security.auth.userdetails.CustomUserDetails;
import com.frog.travelwithme.intergration.BaseIntegrationTest;
import com.frog.travelwithme.utils.ObjectMapperUtils;
import com.frog.travelwithme.utils.ResultActionsUtils;
import com.frog.travelwithme.utils.StubData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class FeedIntegrationTest extends BaseIntegrationTest {

    private final String BASE_URL = "/feed";

    private final String EMAIL = StubData.MockMember.getEmail();

    private final String TAG_NAME = StubData.MockFeed.getTagName();

    private final int SIZE = StubData.MockFeed.getSize();

    private long feedId;

    @Autowired
    private FeedService feedService;

    @Autowired
    private TagService tagService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AES128Config aes128Config;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    public void beforEach() {
        MemberDto.SignUp signUpDto = StubData.MockMember.getSignUpDto();
        memberService.signUp(signUpDto);
        Tag tagOne = Tag.builder().name(TAG_NAME + "1").build();
        tagRepository.save(tagOne);
        Tag tagTwo = Tag.builder().name(TAG_NAME + "2").build();
        tagRepository.save(tagTwo);
        FeedDto.Post postDto = StubData.MockFeed.getPostDto();
        FeedDto.Response response = feedService.postFeed(this.EMAIL, postDto);
        feedId = response.getId();
    }

    @Test
    @DisplayName("피드 작성")
    void feedControllerTest1() throws Exception {
        // given
        feedService.deleteFeed(EMAIL, feedId);
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        FeedDto.Post postDto = StubData.MockFeed.getPostDto();

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL)
                .build().toUri().toString();
        String json = ObjectMapperUtils.asJsonString(postDto);
        ResultActions actions = ResultActionsUtils.postRequestWithContentAndToken(
                mvc, uri, json, accessToken, encryptedRefreshToken);

        // then
        FeedDto.Response response = ObjectMapperUtils.actionsSingleToResponseWithData(
                actions, FeedDto.Response.class);
        assertThat(response.getContents()).isEqualTo(postDto.getContents());
        assertThat(response.getTags()).isEqualTo(postDto.getTags());
        assertThat(response.getLocation()).isEqualTo(postDto.getLocation());
        assertThat(response.getCommentCount()).isZero();
        assertThat(response.getLikeCount()).isZero();
        assertThat(response.getNickname()).isNotNull();
        actions
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("피드 조회")
    void feedControllerTest2() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId)
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.getRequestWithToken(mvc, uri, accessToken, encryptedRefreshToken);

        // then
        FeedDto.Response response = ObjectMapperUtils.actionsSingleToResponseWithData(
                actions, FeedDto.Response.class);
        assertThat(response.getId()).isNotNull();
        assertThat(response.getContents()).isNotNull();
        assertThat(response.getNickname()).isNotNull();
        assertThat(response.getTags()).isNotNull();
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모든 피드 조회")
    void feedControllerTest3() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL)
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.getRequestWithToken(
                mvc, uri, accessToken, encryptedRefreshToken);

        // then
        // TODO: actionsMultiToResponseWithData 필요
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("피드 수정")
    void feedControllerTest4() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        FeedDto.Patch patchDto = StubData.MockFeed.getPatchDto();

        // when
        String json = ObjectMapperUtils.asJsonString(patchDto);
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId)
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.patchRequestWithContentAndToken(
                mvc, uri, json, accessToken, encryptedRefreshToken);

        // then
        FeedDto.Response response = ObjectMapperUtils.actionsSingleToResponseWithData(
                actions, FeedDto.Response.class);
        assertThat(response.getContents()).isEqualTo(patchDto.getContents());
        assertThat(response.getTags()).isEqualTo(response.getTags());
        assertThat(response.getLocation()).isEqualTo(patchDto.getLocation());
        assertThat(response.getCommentCount()).isZero();
        assertThat(response.getLikeCount()).isZero();
        assertThat(response.getNickname()).isNotNull();
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("피드 삭제")
    void feedControllerTest5() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/" + feedId)
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.deleteRequestWithToken(
                mvc, uri, accessToken, encryptedRefreshToken);

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Tag Name과 유사한 모든 TAG 조회")
    void feedControllerTest6() throws Exception {
        // given
        CustomUserDetails userDetails = StubData.MockMember.getUserDetails();
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();
        String encryptedRefreshToken = aes128Config.encryptAes(refreshToken);
        MultiValueMap<String, String> tagNamePapram = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> sizeParam = new LinkedMultiValueMap<>();
        tagNamePapram.add(TAG_NAME, TAG_NAME);
        sizeParam.add("size", String.valueOf(SIZE));
        log.info("tagOne : {}", tagRepository.findAll().get(0).getName());

        // when
        String uri = UriComponentsBuilder.newInstance().path(BASE_URL + "/tags")
                .build().toUri().toString();
        ResultActions actions = ResultActionsUtils.getRequestWithTokenAndTwoParams(
                mvc, uri, accessToken, encryptedRefreshToken, tagNamePapram, sizeParam);

        // then
        // TODO: actionsMultiToResponseWithData 필요
        actions
                .andExpect(status().isOk());
    }
}
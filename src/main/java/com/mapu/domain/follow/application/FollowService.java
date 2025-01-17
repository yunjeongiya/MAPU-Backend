package com.mapu.domain.follow.application;

import com.mapu.domain.follow.application.response.FollowListResponseDTO;
import com.mapu.domain.follow.application.response.FollowUserDTO;
import com.mapu.domain.follow.dao.FollowRepository;
import com.mapu.domain.follow.domain.Follow;
import com.mapu.domain.follow.exception.FollowException;
import com.mapu.domain.follow.exception.errorcode.FollowExceptionErrorCode;
import com.mapu.domain.user.dao.UserRepository;
import com.mapu.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    /**
     * 유저 팔로우 API 서비스
     * 유저 팔로우 API 서비스
     */
    public void followUser(Long followerId, Long followingId) {
        // 팔로워와 팔로잉 ID가 같으면 예외 발생
        if (followerId.equals(followingId)) {
            throw new FollowException(FollowExceptionErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }

        User follower = getUserById(followerId);
        User following = getUserById(followingId);

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new FollowException(FollowExceptionErrorCode.ALREADY_FOLLOWING);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();
        followRepository.save(follow);
    }

    /**
     * 언팔로우 API 서비스
     */
    public void unfollowUser(Long followerId, Long followingId) {
        // 팔로워와 팔로잉 ID가 같을 때
        if (followerId.equals(followingId)) {
            throw new FollowException(FollowExceptionErrorCode.SELF_UNFOLLOW_NOT_ALLOWED);
        }

        User follower = getUserById(followerId);
        User following = getUserById(followingId);

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new FollowException(FollowExceptionErrorCode.NOT_FOLLOWING));

        followRepository.delete(follow);
    }

    /**
     * 사용자의 팔로워 목록 조회
     */
    public FollowListResponseDTO getFollowers(Long userId) {
        User user = getUserById(userId);
        List<Follow> followers = followRepository.findByFollowing(user);

        List<FollowUserDTO> followerDTOs = followers.stream()
                .map(follow -> FollowUserDTO.builder()
                        .userId(follow.getFollower().getId())
                        .nickname(follow.getFollower().getNickname())
                        .imgUrl(follow.getFollower().getImage())
                        .profileId(follow.getFollower().getProfileId())
                        .build())
                .collect(Collectors.toList());

        return FollowListResponseDTO.builder()
                .userId(userId)
                .users(followerDTOs)
                .build();
    }

    /**
     * 팔로잉 목록 조회
     */
    public FollowListResponseDTO getFollowing(Long userId) {
        User user = getUserById(userId);
        List<Follow> following = followRepository.findByFollower(user);

        List<FollowUserDTO> followingDTOs = following.stream()
                .map(follow -> FollowUserDTO.builder()
                        .userId(follow.getFollowing().getId())
                        .nickname(follow.getFollowing().getNickname())
                        .imgUrl(follow.getFollowing().getImage())
                        .profileId(follow.getFollowing().getProfileId())
                        .build())
                .collect(Collectors.toList());

        return FollowListResponseDTO.builder()
                .userId(userId)
                .users(followingDTOs)
                .build();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new FollowException(FollowExceptionErrorCode.FOLLOW_NOT_FOUND));
    }
}
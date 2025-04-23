package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private UserService userService;

        private User testUser;

        @BeforeEach
        void setUp() {
                testUser = new User();
                testUser.setId(1L);
                testUser.setUsername("testuser");
                testUser.setPassword("password123");
                testUser.setEmail("test@example.com");
                testUser.setPhone("13800000000");
                testUser.setStatus(1);
        }

        @Test
        void whenFindByUsername_thenReturnUser() {
                // 准备测试数据
                when(userRepository.findByUsername(testUser.getUsername()))
                                .thenReturn(Optional.of(testUser));

                // 执行测试
                Optional<User> found = userService.findByUsername(testUser.getUsername());

                // 验证结果
                assertTrue(found.isPresent());
                assertEquals(testUser.getUsername(), found.get().getUsername());
                verify(userRepository).findByUsername(testUser.getUsername());
        }

        @Test
        void whenSaveUser_thenReturnSavedUser() {
                // 准备测试数据
                when(passwordEncoder.encode(testUser.getPassword()))
                                .thenReturn("encodedPassword");
                when(userRepository.save(any(User.class)))
                                .thenReturn(testUser);

                // 执行测试
                User savedUser = userService.save(testUser);

                // 验证结果
                assertNotNull(savedUser);
                assertEquals(testUser.getUsername(), savedUser.getUsername());
                verify(passwordEncoder).encode(testUser.getPassword());
                verify(userRepository).save(any(User.class));
        }

        @Test
        void whenUpdateUser_thenReturnUpdatedUser() {
                // 准备测试数据
                when(userRepository.findById(testUser.getId()))
                                .thenReturn(Optional.of(testUser));
                when(userRepository.save(any(User.class)))
                                .thenReturn(testUser);

                // 修改用户信息
                testUser.setEmail("newemail@example.com");

                // 执行测试
                User updatedUser = userService.update(testUser);

                // 验证结果
                assertNotNull(updatedUser);
                assertEquals("newemail@example.com", updatedUser.getEmail());
                verify(userRepository).findById(testUser.getId());
                verify(userRepository).save(any(User.class));
        }

        @Test
        void whenDeleteUser_thenVerifyDeletion() {
                // 准备测试数据
                when(userRepository.findById(testUser.getId()))
                                .thenReturn(Optional.of(testUser));
                doNothing().when(userRepository).deleteById(testUser.getId());

                // 执行测试
                userService.delete(testUser.getId());

                // 验证结果
                verify(userRepository).deleteById(testUser.getId());
        }

        @Test
        void whenChangePassword_thenVerifyPasswordChange() {
                // 准备测试数据
                String newPassword = "newPassword123";
                when(userRepository.findById(testUser.getId()))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.encode(newPassword))
                                .thenReturn("encodedNewPassword");
                when(userRepository.save(any(User.class)))
                                .thenReturn(testUser);

                // 执行测试
                userService.changePassword(testUser.getId(), newPassword);

                // 验证结果
                verify(passwordEncoder).encode(newPassword);
                verify(userRepository).save(any(User.class));
        }
}
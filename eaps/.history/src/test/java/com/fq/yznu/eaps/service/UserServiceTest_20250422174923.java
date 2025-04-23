package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.mapper.UserMapper;
import com.fq.yznu.eaps.service.impl.UserServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

        @Mock
        private UserMapper userMapper;

        @Mock
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private UserServiceImpl userService;

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
                when(userMapper.selectOne(any()))
                                .thenReturn(testUser);

                Optional<User> found = userService.findByUsername(testUser.getUsername());

                assertTrue(found.isPresent());
                assertEquals(testUser.getUsername(), found.get().getUsername());
                verify(userMapper).selectOne(any());
        }

        @Test
        void whenSaveUser_thenReturnSavedUser() {
                when(passwordEncoder.encode(anyString()))
                                .thenReturn("encodedPassword");
                when(userMapper.insert(any(User.class)))
                                .thenReturn(1);

                User savedUser = userService.save(testUser);

                assertNotNull(savedUser);
                assertEquals(testUser.getUsername(), savedUser.getUsername());
                verify(passwordEncoder).encode(testUser.getPassword());
                verify(userMapper).insert(any(User.class));
        }

        @Test
        void whenUpdateUser_thenReturnUpdatedUser() {
                when(userMapper.updateById(any(User.class)))
                                .thenReturn(1);

                testUser.setEmail("newemail@example.com");
                User updatedUser = userService.update(testUser);

                assertNotNull(updatedUser);
                assertEquals("newemail@example.com", updatedUser.getEmail());
                verify(userMapper).updateById(any(User.class));
        }

        @Test
        void whenDeleteUser_thenVerifyDeletion() {
                when(userMapper.deleteById(testUser.getId()))
                                .thenReturn(1);

                userService.delete(testUser.getId());

                verify(userMapper).deleteById(testUser.getId());
        }

        @Test
        void whenChangePassword_thenVerifyPasswordChange() {
                String newPassword = "newPassword123";
                when(userMapper.selectById(testUser.getId()))
                                .thenReturn(testUser);
                when(passwordEncoder.encode(anyString()))
                                .thenReturn("encodedNewPassword");
                when(userMapper.updateById(any(User.class)))
                                .thenReturn(1);

                userService.changePassword(testUser.getId(), newPassword);

                verify(passwordEncoder).encode(newPassword);
                verify(userMapper).updateById(any(User.class));
        }
}
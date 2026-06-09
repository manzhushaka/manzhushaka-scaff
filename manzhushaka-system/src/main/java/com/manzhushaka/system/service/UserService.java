package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.user.UserForm;
import com.manzhushaka.system.dto.user.UserQuery;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.user.UserVO;

public interface UserService {
    PageResult<UserVO> page(UserQuery query);

    UserVO getById(Long id);

    Long create(UserForm form);

    void update(Long id, UserForm form);

    void delete(Long id);
}

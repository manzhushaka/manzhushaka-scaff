package com.manzhushaka.db.crypto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DemoEncryptedRecordMapper extends BaseMapper<DemoEncryptedRecord> {

    @Select("""
        select id, username, mobile_ciphertext
        from demo_encrypted_record
        where username = #{username}
        limit 1
        """)
    @Results(id = "demoEncryptedRecordResultMap", value = {
        @Result(property = "id", column = "id", id = true),
        @Result(property = "username", column = "username"),
        @Result(property = "mobile", column = "mobile_ciphertext", typeHandler = EncryptStringTypeHandler.class)
    })
    DemoEncryptedRecord selectByUsername(@Param("username") String username);
}

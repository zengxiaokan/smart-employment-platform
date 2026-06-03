package com.itzk.SmartEmploymentPlatform.mapper;

import com.itzk.SmartEmploymentPlatform.pojo.entry.User;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.LoginFromDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.UserInfoVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminHrVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminUserVO;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    User getUserById(Long id) ;

    @Select("SELECT * FROM users WHERE phone = #{phone} AND role = #{role} AND password = #{password} AND status = 0")
    User selectBypassword(LoginFromDTO loginFromDTO);

    @Select("SELECT * FROM users WHERE username = #{username} ")
    User getByusername(String username);

    @Select("SELECT * FROM users WHERE phone = #{phone}")
    User getByPhone(String phone);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO users (username, phone, password, nickname, role, status ,created_at,company_id) " +
            "VALUES (#{username}, #{phone}, #{password}, #{nickname}, #{role}, 0,now(),#{companyId})")
    void insert(User user);

    void updateByInfo(UserInfoVo userInfoVo);

    @Update("update users set avatar_url = #{avatarUrl} where id = #{userId}")
    void uodataAvatar(Long userId, String avatarUrl);


    @Update(("update users set company_id = #{companyId} where id = #{id}"))
    void updateCompanyId(Long id,Long companyId);

    @Select("select * from users where id = #{userId}")
    User getByuserId(Long userId);

    void update(User user);

    @Select("SELECT COUNT(*) FROM users WHERE role = 0 AND status = 0")
    Long countJobSeekers();

    @Select("SELECT role, COUNT(*) AS count FROM users WHERE status = 0 GROUP BY role")
    List<Map<String, Object>> countByRole();

    @Select("SELECT DATE(created_at) AS date, COUNT(*) AS count FROM users WHERE created_at >= #{since} GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> countNewUsersByDate(@Param("since") java.time.LocalDateTime since);

    @Select("SELECT id, username, nickname, phone, role, created_at FROM users ORDER BY created_at DESC LIMIT #{limit}")
    List<Map<String, Object>> getRecentUsers(@Param("limit") int limit);

    /** 管理员用户列表：非 HR 用户，管理员排前面 */
    List<AdminUserVO> selectAdminUsers(@Param("username") String username,
                                       @Param("keyword") String keyword,
                                       @Param("role") Integer role,
                                       @Param("status") Integer status);

    /** HR 列表：role=1，含审核状态 */
    List<AdminHrVO> selectAdminHrs(@Param("name") String name,
                                   @Param("auditStatus") Integer auditStatus,
                                   @Param("status") Integer status);

    @Insert("INSERT INTO users (username, password, phone, realname, nickname, city, role, status, created_at) " +
            "VALUES (#{username}, #{password}, #{phone}, #{realname}, #{nickname}, #{city}, #{role}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAdmin(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM users WHERE phone = #{phone}")
    int countByPhone(@Param("phone") String phone);

    @Select("SELECT id FROM users WHERE role = 2 AND status = 0")
    List<Long> selectAdminIds();
}

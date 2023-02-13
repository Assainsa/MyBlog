package com.lintao.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lintao.blog.dao.mapper.SysUserMapper;
import com.lintao.blog.dao.pojo.SysUser;
import com.lintao.blog.service.SysUserService;
import com.lintao.blog.service.TokenService;
import com.lintao.blog.utils.UserThreadLocal;
import com.lintao.blog.vo.ErrorCode;
import com.lintao.blog.vo.LoginUserVo;
import com.lintao.blog.vo.Result;
import com.lintao.blog.vo.UserVo;
import com.lintao.blog.vo.params.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String salt = "mszlu!@#"; //登录加密盐，用来加强password的md5加密效果
    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser==null){
            sysUser=new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar("/static/img/logo.b3a48c0.png");
            sysUser.setNickname("无");
        }
        return sysUser;
    }

    @Override
    public SysUser findUser(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword,password);
        queryWrapper.select(SysUser::getAccount,SysUser::getId,SysUser::getAvatar,SysUser::getNickname,SysUser::getAdmin);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Result findUserByToken(String token) {
        SysUser sysUser = tokenService.checkToken(token);
        if (sysUser == null) {
            return Result.fail(ErrorCode.TOKEN_INVALID.getCode(), ErrorCode.TOKEN_INVALID.getMsg());
        }
        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setId(sysUser.getId());
        loginUserVo.setNickname(sysUser.getNickname());
        loginUserVo.setAccount(sysUser.getAccount());
        loginUserVo.setAvatar(sysUser.getAvatar());
        loginUserVo.setAdmin(sysUser.getAdmin());
        return Result.success(loginUserVo);
    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(SysUser sysUser) {
        //保存用户时id会自动生成，这个地方默认的id时分布式id，采用雪花算法
        sysUserMapper.insert(sysUser);
    }

    @Override
    public UserVo findUserVoById(Long authorId) {
        SysUser user = findUserById(authorId);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user,userVo);
        return userVo;
    }

    @Override
    public Result updateUser(LoginParam user, String token) {
        /**
         * 1. 判断参数是否合法(交给前端来判断)
         * 2. 如果传来的用户名和昵称为空，则说明这两个属性不需要改
         * 3. 如果不为空，判断在数据库中是否已存在
         * 4. 如果不存在，则更新
         */

        String account = user.getAccount();
        String password = user.getPassword();
        String nickname = user.getNickname();
        String avatar = user.getAvatar();
        SysUser updateUser = new SysUser();
        if (!StringUtils.isBlank(account)) {
            SysUser existUser = findUserByAccount(account);
            if (existUser != null) {
                return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(), ErrorCode.ACCOUNT_EXIST.getMsg());
            }else {
                updateUser.setAccount(user.getAccount());
            }
        }

        if (!StringUtils.isBlank(nickname)) {
            SysUser existUser = findUserByNickName(nickname);
            if (existUser != null) {
                return Result.fail(ErrorCode.NICKNAME_EXIST.getCode(), ErrorCode.NICKNAME_EXIST.getMsg());
            }else {
                updateUser.setNickname(user.getNickname());
            }
        }
        //要从threadlocal里拿用户信息，所以该controller方法要放入登录拦截器中
        updateUser.setId(UserThreadLocal.get().getId());
        updateUser.setPassword(DigestUtils.md5Hex(user.getPassword()+salt));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        updateUser.setLastLogin(Long.valueOf(dateFormat.format(System.currentTimeMillis())));
        if (!StringUtils.isBlank(avatar)){
            updateUser.setAvatar(user.getAvatar());
        }
        sysUserMapper.updateById(updateUser);
        //更新ThreadLocal
        SysUser sysUser = sysUserMapper.selectById(UserThreadLocal.get().getId());
        UserThreadLocal.remove();
        UserThreadLocal.put(sysUser);
        //更新token
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS); //这里用fastJSON来转换用户信息
        return Result.success(null);
    }

    @Override
    public SysUser findUserByNickName(String nickname) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getNickname,nickname);
        return sysUserMapper.selectOne(queryWrapper);
    }
}

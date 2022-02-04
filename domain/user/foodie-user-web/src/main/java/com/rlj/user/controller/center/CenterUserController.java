package com.rlj.user.controller.center;

import com.rlj.controller.BaseController;
import com.rlj.pojo.IMOOCJSONResult;
import com.rlj.user.pojo.Users;
import com.rlj.user.pojo.bo.center.CenterUserBO;
import com.rlj.user.pojo.vo.UsersVO;
import com.rlj.user.resource.FileUpload;
import com.rlj.user.service.center.CenterUserService;
import com.rlj.utils.CookieUtils;
import com.rlj.utils.DateUtil;
import com.rlj.utils.JsonUtils;
import com.rlj.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Api(value = "用户信息接口",tags = {"用户信息相关接口"})
@RequestMapping("userInfo")
@RestController//该注解让返回的所有请求都是json对象
public class CenterUserController extends BaseController {
    @Autowired
    private CenterUserService centerUserService;
    @Autowired
    private FileUpload fileUpload;
    @Autowired
    private RedisOperator redisOperator;
    @ApiOperation(value = "修改用户信息",notes = "修改用户信息",httpMethod = "POST")
    @PostMapping("update")//前端center中查询用户信息的路由就是center/userInfo
    public IMOOCJSONResult update(@RequestParam String userId, @RequestBody @Valid CenterUserBO centerUserBO,
                                  BindingResult result,
                                  HttpServletRequest request, HttpServletResponse response){
        Users userNewResult = centerUserService.updateUserInfo(userId,centerUserBO);
        //获取通过注解验证的结果信息，如果有直接return
        if (result.hasErrors()){
            Map<String, String> errorMap = getErrors(result);
            return IMOOCJSONResult.errorMap(errorMap);
        }

        //新的用户信息也要覆盖本地的Cookie，和我们PassportController的手段一样
        //userNewResult = setNullProperty(userNewResult);
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USR_TOKEN+":"+userNewResult.getId(),uniqueToken);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userNewResult,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(usersVO),true);
        return IMOOCJSONResult.ok();
    }
    @ApiOperation(value = "用户头像修改",notes = "用户头像修改",httpMethod = "POST")
    @PostMapping("uploadFace")//前端center中查询用户信息的路由就是center/userInfo
    public IMOOCJSONResult uploadFace(@RequestParam String userId, MultipartFile file,
                                    HttpServletRequest request, HttpServletResponse response){
        //文件路径=文件上上级目录地址+以用户ID为名的目录地址+文件名
        //1、文件上上级目录地址
        //String fileSpace = IMAGE_USER_FACE_LOCATION;
        String fileSpace = fileUpload.getImageUserFaceLocation();
        //2、以用户ID为名的目录地址(File.separator等价于"/")
        String uploadPathPrefix = File.separator +userId;
        //根据传来的文件file，按规格重塑文件名，并上传该文件到拼接的路径中，并按重塑文件名命名
        if (file != null){
            FileOutputStream fileOutputStream = null;
            try {//整体try/catch：command+option+T
                //3、文件名
                String fileName = file.getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)){
                    //3.1、得到文件后缀
                    String fileNameArr[] = fileName.split("\\.");
                    String suffix = fileNameArr[fileNameArr.length - 1];
                    //3.1.1、判断后缀名是否为指定的图片格式
                    if (!suffix.equalsIgnoreCase("png")&&
                    !suffix.equalsIgnoreCase("jpg")&&
                    !suffix.equalsIgnoreCase("jpeg")){
                        return IMOOCJSONResult.errorMsg("图片格式不正确");
                    }
                    //3.2、文件名称按规格重塑(这是覆盖式上传，可以增量，比如说后面再拼接一个当前时间)
                    String newFileName = "face-"+userId+"."+suffix;
                    //4、得到最终文件路径，在该路径下创建目录
                    String finalFacePath = fileSpace + uploadPathPrefix + File.separator + newFileName;
                    //***给下面获取本地路径用的
                    uploadPathPrefix += ("/" + newFileName);
                    File outFile = new File(finalFacePath);
                    //解析这个目录，创建文件夹，只要某级目录的上一级目录还能在路径中找到，就一直递归生成
                    if (outFile.getParentFile() != null){
                        outFile.getParentFile().mkdirs();
                    }
                    //获取到传入文件file的输入流，通过我们文件目录创建的输出流导入到该目录下
                    fileOutputStream = new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    IOUtils.copy(inputStream,fileOutputStream);//import org.apache.commons.io.IOUtils;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null){
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            return IMOOCJSONResult.errorMsg("文件不能为空");
        }
        //获取图片上层目录在浏览器的访问地址
        String imageServerUrl = fileUpload.getImageServerUrl();
        //由于浏览器可能存在缓存的情况(因为是覆盖式，所以之前上传的图片路径、名字实际上和现在一模一样)，
        //所以在这里加上时间戳来保证更新后的图片可以及时刷新
        String finalUserFaceUrl = imageServerUrl+uploadPathPrefix + "?t="
                + DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);
        //更新用户头像到数据库
        Users userResult = centerUserService.updateUserFace(userId,finalUserFaceUrl);
        //新的用户信息也要覆盖本地的Cookie，和我们PassportController的手段一样
        userResult = setNullProperty(userResult);
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USR_TOKEN+":"+userResult.getId(),uniqueToken);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(usersVO),true);
        return IMOOCJSONResult.ok();
    }
    //定义一个清空查询到的Users对象的私密属性的方法
    private Users setNullProperty(Users userNewResult){
        userNewResult.setPassword(null);
        userNewResult.setMobile(null);
        userNewResult.setEmail(null);
        userNewResult.setCreatedTime(null);
        userNewResult.setUpdatedTime(null);
        userNewResult.setBirthday(null);
        return userNewResult;
    }
    //获取用户表单修改格式错误的提示信息
    private Map<String,String> getErrors(BindingResult result){
        Map<String,String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError error : errorList){
            //发生验证错误所对应的某一个属性
            String errorField = error.getField();
            //验证作物的信息
            String errorMsg = error.getDefaultMessage();
            map.put(errorField,errorMsg);
        }
        return map;
    }
}

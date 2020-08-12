package cn.com.chaochuang.pdf_operation.utils;

import cn.com.chaochuang.pdf_operation.model.AppResponse;

import com.alibaba.fastjson.JSON;
import com.chaochuang.oa.dataaec.util.AesTool;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 2019-7-23
 *
 * @author Shicx
 */
public class DecodeInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        //chain.proceed 得到的是最终响应踢体
        Response response = chain.proceed(chain.request());

        if (response.code() == 200) {
            if (response.body() != null && response.body().contentType() != null) {
                MediaType contentType = response.body().contentType();
                //TODO 没有考虑返回二进制流的情况
                String string = response.body().string();
                AppResponse resData = JSON.parseObject(string, AppResponse.class);
                if (resData.isEncodeFlag() && resData.getData() != null) {
                    resData.setData(AesTool.decode(resData.getData().toString()));
                }
                ResponseBody body = ResponseBody.create(contentType, JSON.toJSONString(resData));
                return response.newBuilder().body(body).build();
            }
        }
        return response;
    }

}

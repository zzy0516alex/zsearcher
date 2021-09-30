package com.Z.NovelReader.Basic;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;


abstract public class IterationThread extends BasicHandlerThread {

    private String StartLink;
    private int MaxIterateNum = 500;
    private int IterateCounter;

    public IterationThread(String startLink, int maxIterateNum) {
        StartLink = startLink;
        IterateCounter = maxIterateNum;
        MaxIterateNum = maxIterateNum;
    }

    public IterationThread(String startLink) {
        StartLink = startLink;
        IterateCounter = MaxIterateNum;
    }

    public String getStartLink() {
        return StartLink;
    }

    @Override
    public void run() {
        super.run();
        try {
            Connection connect = Jsoup.connect(StartLink);
            connect.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
            Document document= connect.get();
            Object o = firstProcess(document);
            if (o == null) throw new RuntimeException("IterationThread：初始结果为null,无法迭代");
            boolean do_break = canBreakIterate(o);
            if (IterateCounter != 0 && !do_break){
                resultProcess();
                StartLink = updateStartLink();
                IterateCounter--;
                Log.d("IterationThread","run:"+(MaxIterateNum-IterateCounter));
                run();
            }else {
                onIterativeFinish();
            }
        } catch (IOException e) {
            e.printStackTrace();
            onErrorOccur(NO_INTERNET);
            report(NO_INTERNET);
        }catch (RuntimeException e){
            e.printStackTrace();
            onErrorOccur(TARGET_NOT_FOUND);
            report(TARGET_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            onErrorOccur(PROCESSOR_ERROR);
            report(PROCESSOR_ERROR);
        }
    }

    /**
     * 实现当前迭代结果的处理过程
     */
    public abstract void resultProcess();

    /**
     * 实现迭代预处理的处理过程
     * @param document 网页
     * @return 预处理结果
     * @throws Exception 预处理过程中的错误
     */
    public abstract Object firstProcess(Document document) throws Exception;

    /**
     * 实现判断是否可以继续迭代
     * @param o 预处理结果
     * @return 判断结论
     */
    public abstract boolean canBreakIterate(Object o);

    /**
     * 更新迭代链接
     * @return 新连接
     */
    public abstract String updateStartLink();

    /**
     * 实现迭代结束后的处理
     */
    public abstract void onIterativeFinish();

    /**
     * 错误处理
     * @param event 错误代码
     */
    public abstract void onErrorOccur(int event);
}
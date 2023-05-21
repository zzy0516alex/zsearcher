package com.Z.NovelReader.Utils;

import android.graphics.Rect;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.Z.NovelReader.Objects.beans.NovelContentPage;
import com.Z.NovelReader.Objects.beans.NovelPageWindow;

import java.util.ArrayList;

public class PageSplit {

    public static ArrayList<NovelContentPage> getPage(@NonNull NovelContentPage tempPage, TextView textView){
        String mContent = tempPage.getPage_content();
        textView.setText(mContent);
        int count=textView.getLineCount();
        int pCount=getPageLineCount(textView);
        int pageNum = (int) Math.ceil((double) ((double) count/(double) pCount));
        ArrayList<NovelContentPage> pages = new ArrayList<>();
        for(int i=0;i < pageNum;i++){
            NovelContentPage current_page = new NovelContentPage();
            int end_pos = mContent.length();
            int start_pos = 0;
            if (i!=0)start_pos = pages.get(i-1).getEnd_pos();
            if (i!=pageNum-1)end_pos = textView.getLayout().getLineEnd((i+1)*pCount-1);
            current_page.setStart_pos(start_pos);
            current_page.setEnd_pos(end_pos);
            current_page.setPage_id(i);
            if (i==0){
                current_page.setFirstPage(true);
                current_page.setTitle(tempPage.getTitle());
            }
            current_page.setPage_content(mContent.substring(start_pos,end_pos));
            current_page.setTempPage(false);
            current_page.setBelong_to_chapID(tempPage.getBelong_to_chapID());
            current_page.setErrorPage(tempPage.isErrorPage());
            pages.add(current_page);
        }
        return pages;
    }

    public static ArrayList<NovelContentPage> getPage(@NonNull String mContent, String title, TextView textView){
        textView.setText(mContent);
        int count=textView.getLineCount();
        int pCount=getPageLineCount(textView);
        int pageNum = (int) Math.ceil((double) ((double) count/(double) pCount));
        ArrayList<NovelContentPage> pages = new ArrayList<>();
        for(int i=0;i < pageNum;i++){
            NovelContentPage current_page = new NovelContentPage();
            int end_pos = mContent.length();
            int start_pos = 0;
            if (i!=0)start_pos = pages.get(i-1).getEnd_pos();
            if (i!=pageNum-1)end_pos = textView.getLayout().getLineEnd((i+1)*pCount-1);
            current_page.setStart_pos(start_pos);
            current_page.setEnd_pos(end_pos);
            current_page.setPage_id(i);
            if (i==0){
                current_page.setFirstPage(true);
                current_page.setTitle(title);
            }
            current_page.setPage_content(mContent.substring(start_pos,end_pos));
            current_page.setTempPage(false);
            pages.add(current_page);
        }
        return pages;
    }
    public static int getPageLineCount(TextView view)
    {
        /*
        * The first row's height is different from other row.
        */
        int h = view.getBottom() - view.getTop();
        int firstH=getLineHeight(0,view);
        int otherH=getLineHeight(1,view);
        int lines = 1;
        if (otherH!=0)lines = (h-firstH)/otherH + 1;//仅一行时返回1
        return lines;
    }

    public static int getLineHeight(int line,TextView view)
    {
        Rect rect=new Rect();
        view.getLineBounds(line,rect);
        return rect.bottom-rect.top;
    }

    public static int getLineHeight(int line,StaticLayout view)
    {
        Rect rect=new Rect();
        view.getLineBounds(line,rect);
        return rect.bottom-rect.top;
    }

    public static int getTextViewLines(TextView textView, int textViewWidth) {

        int width = textViewWidth - textView.getCompoundPaddingLeft() - textView.getCompoundPaddingRight();

        StaticLayout staticLayout;

        staticLayout = getStaticLayout23(textView, width);

        int lines_without_limit = staticLayout.getLineCount();

        textView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int h = textView.getMeasuredHeight();
        int h2 = textView.getHeight();
        int height = staticLayout.getHeight();
        int firstH=getLineHeight(0,staticLayout);
        int otherH=getLineHeight(1,staticLayout);
        int lines_in_page = (h - firstH) / otherH + 1;
        int lineEnd = staticLayout.getLineEnd(22);

        //int maxLines = textView.getMaxLines();

        return lines_in_page;

    }

    /**

     * sdk>=23

     */

    @RequiresApi(api = Build.VERSION_CODES.M)

    private static StaticLayout getStaticLayout23(TextView textView, int width) {

        StaticLayout.Builder builder = StaticLayout.Builder.obtain(textView.getText(),

                0, textView.getText().length(), textView.getPaint(), width)

                .setAlignment(Layout.Alignment.ALIGN_NORMAL)

                .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)

                .setLineSpacing(textView.getLineSpacingExtra(), textView.getLineSpacingMultiplier())

                .setIncludePad(textView.getIncludeFontPadding())

                .setBreakStrategy(textView.getBreakStrategy())

                .setHyphenationFrequency(textView.getHyphenationFrequency())

                .setMaxLines(textView.getMaxLines() == -1 ? Integer.MAX_VALUE : textView.getMaxLines());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setJustificationMode(textView.getJustificationMode());

        }

        if (textView.getEllipsize() != null && textView.getKeyListener() == null) {

            builder.setEllipsize(textView.getEllipsize())

                    .setEllipsizedWidth(width);

        }

        return builder.build();

    }

    /**

     * sdk<23

     */

    private static StaticLayout getStaticLayout(TextView textView, int width) {

        return new StaticLayout(textView.getText(),

                0, textView.getText().length(),

                textView.getPaint(), width, Layout.Alignment.ALIGN_NORMAL,

                textView.getLineSpacingMultiplier(),

                textView.getLineSpacingExtra(), textView.getIncludeFontPadding(), textView.getEllipsize(),

                width);

    }
}

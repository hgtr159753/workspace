package com.smsf.recordtrancharacter.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.smsf.recordtrancharacter.Adapter.UploadLogsAdapter;
import com.smsf.recordtrancharacter.R;
import com.smsf.recordtrancharacter.TranslateActivity;
import com.smsf.recordtrancharacter.audio.WavHelper;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;
import me.curzbin.library.BottomDialog;
import me.curzbin.library.Item;
import me.curzbin.library.OnItemClickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class historyFrag extends Fragment {
    ListView listv;
    ArrayList<String> voicesname;
    ArrayAdapter<String> adapter;
    private static String path = "";
    private String FILE_PATH = "/storage/emulated/0/Smvoice/voice";
    private List<File> fileArrayList = new ArrayList<>();
    private List<String> fileNameArrayList = new ArrayList<>();
    private UploadLogsAdapter uploadLogsAdapter;
    private String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Smvoice/voice/";
    private LinearLayout not_content_menu;
    private boolean isFristShow;
    private View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            Log.e("666", "HomeFragment");
            mRootView = inflater.inflate(R.layout.historyvoice, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        listv = mRootView.findViewById(R.id.voicelist);
        not_content_menu = mRootView.findViewById(R.id.not_content_menu);

        //getFilePath();
        return mRootView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFristShow = true;
    }

    public void getFilePath() {
        loadPathFile();
        if (uploadLogsAdapter ==null){
            uploadLogsAdapter = new UploadLogsAdapter(fileNameArrayList, fileArrayList, getActivity());
            listv.setAdapter(uploadLogsAdapter);
        }
        uploadLogsAdapter.setOnClickLinter(new UploadLogsAdapter.onClickLinter() {
            @Override
            public void onClick(int index) {
                // 点击more
                BottomDialog bottomDialog = new BottomDialog(getActivity());
                bottomDialog.title(R.string.title_item)
                        .layout(BottomDialog.GRID)
                        .orientation(BottomDialog.VERTICAL)
                        .inflateMenu(R.menu.menu_grid, new OnItemClickListener() {
                            @Override
                            public void click(Item item) {
                                switch (item.getId()) {
                                    case R.id.moments:
                                        // 重命名
                                        final EditText inputServer = new EditText(getActivity());
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        path = fileNameArrayList.get(index);
                                        String fileName = path.substring(0, path.length() - 4);
                                        inputServer.setText(fileName);
                                        builder.setTitle("编辑新文件名").setView(inputServer)
                                                .setNegativeButton("取消", (dialogInterface, i) -> {
                                                    //取消重新选择

                                                });
                                        builder.setPositiveButton("保存", (dialog, which) -> {
                                            String textName = inputServer.getText().toString();
                                            if (!TextUtils.isEmpty(textName)) {
                                                FixFileName(savePath + path, textName);
                                                Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
                                                //fileNameArrayList.set(index,textName);
                                                loadPathFile();
                                                bottomDialog.dimmils();
                                                uploadLogsAdapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(getActivity(), "文件名称不能为空", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        builder.show();
                                        break;
                                    case R.id.share_btn:
                                        // 分享
                                        new Share2.Builder(getActivity())
                                                // 指定分享的文件类型
                                                .setContentType(ShareContentType.AUDIO)
                                                // 设置要分享的文件 Uri
                                                .setShareFileUri(Uri.parse(savePath + fileNameArrayList.get(index)))
                                                // 设置分享选择器的标题
                                                .setTitle("Share Image")
                                                .build()
                                                // 发起分享
                                                .shareBySystem();

                                        break;
                                    case R.id.delect_btn:
                                        // 删除
                                        try {
                                            String voicename = fileNameArrayList.get(index);
                                            File f = new File(voicename);
                                            String nfile = savePath + f.getName();
                                            File df = new File(nfile);
                                            String npname = f.getName().replace(".wav", ".pcm");
                                            df.delete();
                                            nfile = savePath + npname;
                                            df = new File(nfile);
                                            df.delete();
                                            loadPathFile();
                                            uploadLogsAdapter.notifyDataSetChanged();
                                            bottomDialog.dimmils();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case R.id.share_voice:
                                        // 转文字
                                        bottomDialog.dimmils();
                                        String voicename = fileNameArrayList.get(index);
                                        File f = new File(voicename);
                                        Intent intent = new Intent(getActivity(), SpeechTranscriberActivity.class);
                                        intent.putExtra("filename", f.getName());
                                        startActivity(intent);
                                        break;
                                    case R.id.share_translate:
                                        //  去翻译
                                        bottomDialog.dimmils();
                                        Intent intentTranslate = new Intent(getActivity(), TranslateActivity.class);
                                        intentTranslate.putExtra("textResult", "");
                                        startActivity(intentTranslate);
                                        break;
                                }
                            }
                        });
                bottomDialog.show();
            }
        });
    }

    public  void loadPathFile() {
        File file = new File(FILE_PATH);
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        if (fileArrayList != null && fileArrayList.size() > 0) {
            fileArrayList.clear();
            fileNameArrayList.clear();
        }
        //获取文件列表内的所有文件名称的集合
        for (File i : files) {
            if (!i.getName().contains(".pcm")) {
                fileArrayList.add(i);
                fileNameArrayList.add(i.getName());
            }
        }
        if (fileNameArrayList != null && fileNameArrayList.size() > 0) {
            not_content_menu.setVisibility(View.GONE);
            listv.setVisibility(View.VISIBLE);
            if (uploadLogsAdapter !=null){
                uploadLogsAdapter.notifyDataSetChanged();
            }
        } else {
            not_content_menu.setVisibility(View.VISIBLE);
            listv.setVisibility(View.GONE);
        }
    }


    /**
     * 2 * 通过文件路径直接修改文件名
     * 3 *
     * 4 * @param filePath 需要修改的文件的完整路径
     * 5 * @param newFileName 需要修改的文件的名称
     * 6 * @return
     * 7
     */
    public static String FixFileName(String filePath, String newFileName) {
        File f = new File(filePath);
        if (!f.exists()) { // 判断原文件是否存在（防止文件名冲突）
            return null;
        }
        newFileName = newFileName.trim();
        if ("".equals(newFileName) || newFileName == null) // 文件名不能为空
            return null;
        String newFilePath = null;
        if (f.isDirectory()) { // 判断是否为文件夹
            newFilePath = filePath.substring(0, filePath.lastIndexOf("/")) + "/" + newFileName;
        } else {
            newFilePath = filePath.substring(0, filePath.lastIndexOf("/")) + "/" + newFileName
                    + filePath.substring(filePath.lastIndexOf("."));
        }
        File nf = new File(newFilePath);
        try {
            f.renameTo(nf); // 修改文件名
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
        return newFilePath;
    }


    private Runnable playPCMRecord = () -> {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        File f = new File(path);
        try {
            mediaPlayer.setDataSource("/data/data/com.smsf.recordtrancharacter/cache/" + f.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
            System.out.println(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isFristShow) {
            getFilePath();
        }
    }


}


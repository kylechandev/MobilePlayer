// IMusicPlayerService.aidl
package com.fairhand.mobileplayer;

// Declare any non-default types here with import statements

/**
 * 服务的代理类
 */
interface IMusicPlayerService {
        /**
         * 根据位置打开相应的音频文件
         */
        void openAudio(int position);

        /**
         * 播放音乐
         */
        void startPlayMusic();

        /**
         * 暂停音乐
         */
        void pausePlayMusic();

        /**
         * 停止音乐
         */
        void stopPlayMusic();

        /**
         * 获取到当前的播放进度
         */
        int getCurrentPlayProgress();

        /**
         * 获取到当前播放的音频的总时长
         */
        int getCurrentAudioDuration();

        /**
         * 获取到当前播放的音频的名字
         */
        String getCurrentPlayAudioName();

        /**
         * 获取到当前播放的音频的歌手
         */
        String getCurrentPlayAudioArtist();

        /**
         * 获取到准备播放的音频的路径
         */
        String getPreparePlayAudioDataPath();

        /**
         * 播放上一首音乐
         */
        void playPreviousAudio();

        /**
         * 播放下一首音乐
         */
        void playNextAudio();

        /**
         * 设置播放模式
         */
        void setPlayMode(int PLAY_MODE);

        /**
         * 获取到播放模式
         */
        int getPlayMode();

        /**
         * 判断是否正在播放
         */
        boolean isPlaying();

        /**
         * 设置音频进度条拖动播放
         */
        void seekTo(int position);

        /**
          * 获取到ALBUM_ID
          */
        long getAlbumId();

        /**
         * 判断是否播放完毕
         */
        boolean isCompletion();

        /**
         * 通过传入的位置获取到播放的音频的名字
         */
        String getPlayAudioNameForPosition(int position);

}

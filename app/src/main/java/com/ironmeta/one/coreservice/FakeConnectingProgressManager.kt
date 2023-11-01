package com.ironmeta.one.coreservice

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ironmeta.one.config.RemoteConfigManager

class FakeConnectingProgressManager private constructor() {
    private var mWaitingConnectingCountDownTimer: CountDownTimer? = null
    private var mProgressCountDownTimer: CountDownTimer? = null
    private var mListener: ProgressListener? = null
    val stateLiveData = MutableLiveData(FakeConnectionState(FakeConnectionState.STATE_IDLE, 0F))

    fun start(listener: ProgressListener?) {
        mWaitingConnectingCountDownTimer?.cancel()
        mProgressCountDownTimer?.cancel()
        mListener = listener
        val millisInFuture = DEFAULT_WAITING_CONNECTING_DURATION
        mWaitingConnectingCountDownTimer = object : CountDownTimer(millisInFuture, 200) {
            override fun onTick(millisUntilFinished: Long) {
                var progress =
                    ((millisInFuture - millisUntilFinished).toFloat() * DEFAULT_WAITING_CONNECTING_MAX_PROGRESS / millisInFuture)
                stateLiveData.value =
                    FakeConnectionState(FakeConnectionState.STATE_WAITING, progress)
                mListener?.onWaitingForConnecting(progress)
            }

            override fun onFinish() {
                stateLiveData.postValue(FakeConnectionState(FakeConnectionState.STATE_FINISH, 100F))
                mProgressCountDownTimer?.cancel()
                mListener?.onFinish()
            }
        }.apply {
            start()
            stateLiveData.value = FakeConnectionState(FakeConnectionState.STATE_START, 0F)
            mListener?.onStart()
        }
    }

    fun notifyVPNConnected() {
        mWaitingConnectingCountDownTimer?.cancel()
        mProgressCountDownTimer?.cancel()
        val millisInFuture = RemoteConfigManager.getInstance().fakeConnectionDuration
        mProgressCountDownTimer = object : CountDownTimer(millisInFuture, 32) {
            override fun onTick(millisUntilFinished: Long) {
                var progress =
                    DEFAULT_WAITING_CONNECTING_MAX_PROGRESS + ((millisInFuture - millisUntilFinished).toFloat() * (100 - DEFAULT_WAITING_CONNECTING_MAX_PROGRESS) / millisInFuture)
                stateLiveData.value =
                    FakeConnectionState(FakeConnectionState.STATE_CONNECTING, progress)
                mListener?.onProgressAfterConnected(progress)
            }

            override fun onFinish() {
                stateLiveData.postValue(FakeConnectionState(FakeConnectionState.STATE_FINISH, 100F))
                mListener?.onFinish()
            }
        }.apply {
            start()
        }
    }

    fun notifyFinish() {
        mWaitingConnectingCountDownTimer?.cancel()
        mProgressCountDownTimer?.cancel()
        stateLiveData.value = FakeConnectionState(FakeConnectionState.STATE_FINISH, 100F)
        mListener?.onFinish()
    }

    fun isStart(): Boolean {
        return stateLiveData.value?.state == FakeConnectionState.STATE_START
    }

    fun isWaitingForConnecting(): Boolean {
        return stateLiveData.value?.state == FakeConnectionState.STATE_WAITING
    }

    fun isProgressingAfterConnected(): Boolean {
        return stateLiveData.value?.state == FakeConnectionState.STATE_CONNECTING
    }

    fun destroy() {
        mWaitingConnectingCountDownTimer?.cancel()
        mProgressCountDownTimer?.cancel()
    }

    companion object {
        const val DEFAULT_WAITING_CONNECTING_MAX_PROGRESS = 5F
        const val DEFAULT_WAITING_CONNECTING_DURATION = 1010000L
        private var instance: FakeConnectingProgressManager = FakeConnectingProgressManager()

        @Synchronized
        fun getInstance(): FakeConnectingProgressManager {
            return instance
        }
    }
}

interface ProgressListener {
    fun onStart()
    fun onWaitingForConnecting(progress: Float)
    fun onProgressAfterConnected(progress: Float)
    fun onFinish()
}

class FakeConnectionState(var state: Int, var progress: Float) {
    companion object {
        const val STATE_IDLE = 0
        const val STATE_START = 1
        const val STATE_WAITING = 2
        const val STATE_CONNECTING = 3
        const val STATE_FINISH = 4
    }
}
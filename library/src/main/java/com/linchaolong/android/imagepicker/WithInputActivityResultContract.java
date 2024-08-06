package com.linchaolong.android.imagepicker;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WithInputActivityResultContract<I, O> extends ActivityResultContract<Void, O> {
    private final ActivityResultContract<I, O> mContract;
    private final I mInput;

    public WithInputActivityResultContract(ActivityResultContract<I, O> contract, I input) {
        this.mContract = contract;
        this.mInput = input;
    }


    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void i) {
        return mContract.createIntent(context, mInput);
    }

    @Override
    public O parseResult(int i, @Nullable Intent intent) {
        return mContract.parseResult(i, intent);
    }

    @Nullable
    @Override
    public SynchronousResult<O> getSynchronousResult(@NonNull Context context, Void input) {
        return mContract.getSynchronousResult(context, mInput);
    }
}

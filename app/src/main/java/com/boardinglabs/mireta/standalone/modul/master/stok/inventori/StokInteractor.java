package com.boardinglabs.mireta.standalone.modul.master.stok.inventori;

import com.boardinglabs.mireta.standalone.component.network.NetworkService;
import com.boardinglabs.mireta.standalone.component.network.entities.TransactionPost;
import com.boardinglabs.mireta.standalone.component.network.response.ItemsResponse;
import com.boardinglabs.mireta.standalone.component.util.PreferenceManager;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StokInteractor {
    private NetworkService mService;

    public StokInteractor(NetworkService service) {
        mService = service;

    }

    public Observable<ItemsResponse> getStockItems(String businessId) {
        return mService.getStockItems(businessId, "Bearer "+ PreferenceManager.getSessionToken()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    public Observable<ResponseBody> createTransaction(TransactionPost transactionPost, String token) {
        return mService.createTransaction(transactionPost, token).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }
}

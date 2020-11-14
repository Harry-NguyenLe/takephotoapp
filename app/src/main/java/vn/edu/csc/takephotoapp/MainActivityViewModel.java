package vn.edu.csc.takephotoapp;

import android.app.Application;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivityViewModel extends AndroidViewModel {
    private MutableLiveData<Uri> image = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public void setImage(Uri uri) {
        compositeDisposable.add(Observable.defer(() -> Observable.fromArray(uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri1 -> {
                    this.image.setValue(uri1);
                }, throwable -> {
                    Toast.makeText(getApplication(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    public LiveData<Uri> getImage() {
        return image;
    }
}

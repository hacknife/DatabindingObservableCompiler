package com.hacknife.databindingobservablecompiler;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;


import com.hacknife.databinding.annotation.Binding;

@Binding
public class User {
    Integer name;
    String password;

    public Integer getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setName(Integer name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

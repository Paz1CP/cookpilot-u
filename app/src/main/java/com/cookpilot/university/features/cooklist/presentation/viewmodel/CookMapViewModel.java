package com.cookpilot.university.features.cooklist.presentation.viewmodel;

import androidx.lifecycle.ViewModel;
import com.cookpilot.university.features.cooklist.domain.model.ShoppingPlace;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class CookMapViewModel extends ViewModel {
    public Double latitude;
    public double longitude;
    public boolean demonstration;
    public boolean permissionRequested;
    public int selected = -1;
    public boolean listing;
    public List<ShoppingPlace> places = Collections.emptyList();

    public void locate(double latitude, double longitude, boolean demonstration) {
        if (this.latitude != null) return;
        this.latitude = latitude;
        this.longitude = longitude;
        this.demonstration = demonstration;
        places = ShoppingPlace.generate(latitude, longitude, new Random());
    }
}

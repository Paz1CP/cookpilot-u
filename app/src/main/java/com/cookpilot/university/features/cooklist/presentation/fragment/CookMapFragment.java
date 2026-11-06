package com.cookpilot.university.features.cooklist.presentation.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cookpilot.university.R;
import com.cookpilot.university.databinding.FragmentCookMapBinding;
import com.cookpilot.university.features.cooklist.domain.model.ShoppingPlace;
import com.cookpilot.university.features.cooklist.presentation.viewmodel.CookMapViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class CookMapFragment extends Fragment {
    private static final int[] ART = {R.drawable.cookmap_mercado_la_cosecha, R.drawable.cookmap_super_ahorro,
            R.drawable.cookmap_bodega_la_esquina, R.drawable.cookmap_verde_fresco, R.drawable.cookmap_despensa_del_barrio};
    private static final int[] NAMES = {R.string.cookmap_place_0_name, R.string.cookmap_place_1_name,
            R.string.cookmap_place_2_name, R.string.cookmap_place_3_name, R.string.cookmap_place_4_name};
    private static final int[] CATEGORIES = {R.string.cookmap_place_0_category, R.string.cookmap_place_1_category,
            R.string.cookmap_place_2_category, R.string.cookmap_place_3_category, R.string.cookmap_place_4_category};
    private static final int[] DESCRIPTIONS = {R.string.cookmap_place_0_description, R.string.cookmap_place_1_description,
            R.string.cookmap_place_2_description, R.string.cookmap_place_3_description, R.string.cookmap_place_4_description};
    private static final int[] PRICES = {R.string.cookmap_place_0_price, R.string.cookmap_place_1_price,
            R.string.cookmap_place_2_price, R.string.cookmap_place_3_price, R.string.cookmap_place_4_price};
    private static final int[] STATUS = {R.string.cookmap_place_0_status, R.string.cookmap_place_1_status,
            R.string.cookmap_place_2_status, R.string.cookmap_place_3_status, R.string.cookmap_place_4_status};
    private static final int[] CHIPS = {R.array.cookmap_place_0_chips, R.array.cookmap_place_1_chips,
            R.array.cookmap_place_2_chips, R.array.cookmap_place_3_chips, R.array.cookmap_place_4_chips};
    private FragmentCookMapBinding binding;
    private CookMapViewModel state;
    private GoogleMap map;
    private BottomSheetBehavior<View> sheet;
    private final Map<Integer, Marker> markers = new HashMap<>();
    private CancellationTokenSource locationRequest;
    private boolean locating;
    private boolean mapLoaded;
    private boolean mapFailed;
    private int mapGeneration;
    private final Runnable mapTimeout = () -> {
        if (!mapLoaded) {
            showMapError();
        }
    };
    private final ActivityResultLauncher<String[]> permissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (binding == null) return;
                if (hasLocationPermission()) locate(); else showLocationFallback();
            });

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCookMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        state = new ViewModelProvider(this).get(CookMapViewModel.class);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.mapHeader, (header, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars()
                            | androidx.core.view.WindowInsetsCompat.Type.displayCutout());
            header.setPadding(dp(16) + bars.left, dp(16) + bars.top, dp(16) + bars.right, dp(16));
            return insets;
        });
        androidx.core.view.ViewCompat.requestApplyInsets(binding.mapHeader);
        sheet = BottomSheetBehavior.from(binding.placeSheet);
        sheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        sheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (binding == null) return;
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (state.listing) {
                        state.listing = false;
                        if (state.selected >= 0) { showDetails(findPlace(state.selected)); return; }
                    } else clearSelection();
                    updatePlacesButton();
                }
                updatePadding();
            }
            @Override public void onSlide(@NonNull View bottomSheet, float offset) { updatePadding(); }
        });
        binding.mapBack.setImageResource(com.cookpilot.university.core.design.icons.CookIcons.back());
        binding.mapBack.setOnClickListener(v -> ((CookListFragment) requireParentFragment()).closeMap());
        binding.mapPermission.setOnClickListener(v -> requestPermission());
        binding.mapDemo.setOnClickListener(v -> {
            cancelLocation();
            state.locate(-12.0464, -77.0428, true);
            renderPlaces();
        });
        binding.mapRetry.setOnClickListener(v -> initializeMap());
        binding.mapPlaces.setOnClickListener(v -> showListing());
        binding.mapFocusLocation.setOnClickListener(v -> focusUserLocation());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override public void handleOnBackPressed() { back(); }
                });
        view.post(() -> {
            if (binding == null) return;
            sheet.setPeekHeight(Math.min(dp(340), view.getHeight() * 45 / 100));
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.placeSheet.getLayoutParams();
            params.topMargin = Math.max(dp(160), view.getHeight() / 4);
            binding.placeSheet.setLayoutParams(params);
            updatePadding();
        });
        initializeMap();
        if (state.latitude == null) {
            if (hasLocationPermission()) locate();
            else if (!state.permissionRequested) requestPermission();
            else showLocationFallback();
        }
    }

    @Override public void onResume() {
        super.onResume();
        if (binding != null && state.latitude == null && hasLocationPermission() && !locating) locate();
    }

    private void initializeMap() {
        mapGeneration++;
        int generation = mapGeneration;
        map = null;
        markers.clear();
        mapLoaded = false;
        mapFailed = false;
        binding.mapRetry.setVisibility(View.GONE);
        binding.mapActions.setVisibility(View.GONE);
        binding.mapStatus.setVisibility(View.VISIBLE);
        binding.mapStatusText.setText(R.string.cookmap_loading);
        int services = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext());
        if (services != ConnectionResult.SUCCESS) { showMapError(); return; }
        try {
            Fragment previous = getChildFragmentManager().findFragmentById(R.id.mapSurface);
            if (previous != null) getChildFragmentManager().beginTransaction().remove(previous).commitNow();
            SupportMapFragment surface = SupportMapFragment.newInstance(new com.google.android.gms.maps.GoogleMapOptions()
                    .backgroundColor(color(R.color.cook_dark_raised)));
            getChildFragmentManager().beginTransaction().replace(R.id.mapSurface, surface).commitNow();
            surface.getMapAsync(googleMap -> {
                if (binding == null || generation != mapGeneration) return;
                map = googleMap;
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.cookmap_dark));
                map.getUiSettings().setMapToolbarEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.getUiSettings().setCompassEnabled(false);
                map.setOnMapClickListener(point -> dismissSheet());
                map.setOnMarkerClickListener(marker -> {
                    if (marker.getTag() instanceof Integer) selectPlace(findPlace((Integer) marker.getTag()));
                    else focusUserLocation();
                    return true;
                });
                map.setOnMapLoadedCallback(() -> {
                    if (binding == null || generation != mapGeneration) return;
                    boolean recovered = mapFailed;
                    mapLoaded = true;
                    mapFailed = false;
                    binding.getRoot().removeCallbacks(mapTimeout);
                    if (state.latitude != null) binding.mapStatus.setVisibility(View.GONE);
                    else if (!locating) showLocationFallback();
                    if (recovered && state.latitude != null) {
                        if (state.listing) showListing();
                        else if (state.selected >= 0) showDetails(findPlace(state.selected));
                    }
                    updatePlacesButton();
                });
                renderPlaces();
            });
            binding.getRoot().removeCallbacks(mapTimeout);
            binding.getRoot().postDelayed(mapTimeout, 25000);
        } catch (RuntimeException error) {
            showMapError();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (hasLocationPermission()) { locate(); return; }
        if (state.permissionRequested && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + requireContext().getPackageName())));
            return;
        }
        state.permissionRequested = true;
        permissions.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void locate() {
        if (locating || state.latitude != null) return;
        cancelLocation();
        locating = true;
        if (!mapFailed) {
            binding.mapStatus.setVisibility(View.VISIBLE);
            binding.mapStatusText.setText(R.string.cookmap_loading);
            binding.mapPermission.setVisibility(View.GONE);
        }
        locationRequest = new CancellationTokenSource();
        CancellationTokenSource token = locationRequest;
        try {
            CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setMaxUpdateAgeMillis(0).setDurationMillis(15000).build();
            LocationServices.getFusedLocationProviderClient(requireContext())
                    .getCurrentLocation(request, locationRequest.getToken())
                    .addOnCompleteListener(task -> {

                        if (locationRequest != token) return;
                        locating = false;
                        if (binding == null || task.isCanceled() || state.latitude != null) return;
                        if (task.isSuccessful() && task.getResult() != null) {
                            state.locate(task.getResult().getLatitude(), task.getResult().getLongitude(), false);
                            renderPlaces();
                        } else showLocationFallback();
                    });
        } catch (SecurityException error) {
            locating = false;
            showLocationFallback();
        }
    }

    private void showLocationFallback() {
        if (mapFailed) return;
        binding.mapStatus.setVisibility(View.VISIBLE);
        binding.mapStatusText.setText(R.string.cookmap_permission);
        binding.mapPermission.setVisibility(View.VISIBLE);
        binding.mapDemo.setVisibility(View.VISIBLE);
    }

    private void showMapError() {
        if (binding == null) return;
        mapFailed = true;
        binding.mapStatus.setVisibility(View.VISIBLE);
        binding.mapStatusText.setText(R.string.cookmap_error);
        binding.mapPermission.setVisibility(View.GONE);
        binding.mapDemo.setVisibility(View.GONE);
        binding.mapRetry.setVisibility(View.VISIBLE);
        binding.mapActions.setVisibility(View.GONE);
        binding.placeSheet.setVisibility(View.INVISIBLE);
    }

    private LatLng origin() { return new LatLng(state.latitude, state.longitude); }
    private LatLng point(ShoppingPlace place) { return new LatLng(place.latitude, place.longitude); }

    private void renderPlaces() {
        if (binding == null || map == null || state.latitude == null) return;
        if (!mapFailed) binding.mapStatus.setVisibility(View.GONE);
        binding.mapSubtitle.setText(state.demonstration ? R.string.cookmap_example_location : R.string.cookmap_subtitle);
        map.clear();
        markers.clear();
        String user = getString(state.demonstration ? R.string.cookmap_example_location : R.string.cookmap_user);
        map.addMarker(new MarkerOptions().position(origin()).title(user)
                .icon(userMarkerIcon()).anchor(.5f, .9f).zIndex(3));
        LatLngBounds.Builder bounds = new LatLngBounds.Builder().include(origin());
        for (ShoppingPlace place : state.places) {
            Marker marker = map.addMarker(new MarkerOptions().position(point(place)).title(getString(NAMES[place.profile]))
                    .icon(markerIcon(getString(NAMES[place.profile]), place.profile, state.selected == place.profile))
                    .anchor(.5f, 1).zIndex(1));
            if (marker != null) { marker.setTag(place.profile); markers.put(place.profile, marker); }
            bounds.include(point(place));
        }
        binding.getRoot().post(() -> {
            if (binding == null || map == null) return;
            updatePadding();
            fitBounds(bounds.build());
            boolean listing = state.listing;
            if (state.selected >= 0) selectPlace(findPlace(state.selected));
            if (listing) showListing();
        });
        updatePlacesButton();
    }

    private BitmapDescriptor userMarkerIcon() {
        Bitmap image = android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.cookmap_pin);
        int width = dp(64);
        int height = Math.round(width * (float) image.getHeight() / image.getWidth());
        return BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(image, width, height, true));
    }

    private BitmapDescriptor markerIcon(String label, int profile, boolean active) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.outfit_semibold));
        paint.setTextSize(dp(12));
        int width = Math.max((int) paint.measureText(label) + dp(24), dp(64));
        int height = dp(88);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        paint.setColor(color(R.color.cook_dark_floating));
        canvas.drawRoundRect(2, dp(58), width - 2, height - dp(6), dp(12), dp(12), paint);
        float center = width / 2f;
        float radius = dp(26);
        android.graphics.Path circle = new android.graphics.Path();
        circle.addCircle(center, dp(29), radius, android.graphics.Path.Direction.CW);
        canvas.save();
        canvas.clipPath(circle);
        Drawable art = AppCompatResources.getDrawable(requireContext(), ART[profile]);
        if (art != null) {
            art.setBounds((int) (center-radius), dp(3), (int) (center+radius), dp(55));
            art.draw(canvas);
        }
        canvas.restore();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(color(active ? R.color.cook_primary : R.color.cook_gray));
        canvas.drawCircle(center, dp(29), radius, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color(R.color.cook_primary_soft));
        canvas.drawText(label, (width-paint.measureText(label))/2, height-dp(14), paint);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private ShoppingPlace findPlace(int profile) {
        for (ShoppingPlace place : state.places) if (place.profile == profile) return place;
        throw new IllegalArgumentException("Unknown demonstration profile");
    }

    private void focusUserLocation() {
        if (map == null || state.latitude == null) return;
        updatePadding();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(origin(), 16));
    }

    private void selectPlace(ShoppingPlace place) {
        state.listing = false;
        state.selected = place.profile;
        for (ShoppingPlace item : state.places) {
            Marker marker = markers.get(item.profile);
            if (marker != null) {
                marker.setIcon(markerIcon(getString(NAMES[item.profile]), item.profile, item.profile == place.profile));
                marker.setZIndex(item.profile == place.profile ? 4 : 1);
            }
        }
        showDetails(place);
        binding.getRoot().post(() -> {
            if (map != null && binding != null) {
                updatePadding();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(point(place), 16));
            }
        });
    }

    private void showDetails(ShoppingPlace place) {
        LinearLayout content = binding.placeContent;
        content.removeAllViews();
        illustration(content, place.profile, 160);
        text(content, getString(NAMES[place.profile]), R.style.TextAppearance_CookPilot_TitleL);

        text(content, getString(R.string.cookmap_distance) + "  ·  " + distance(place), R.style.TextAppearance_CookPilot_Label);
        text(content, getString(DESCRIPTIONS[place.profile]), R.style.TextAppearance_CookPilot_Body);
        text(content, getString(R.string.cookmap_ideal), R.style.TextAppearance_CookPilot_TitleS);
        chips(content, place.profile, 4);
        text(content, getString(R.string.cookmap_price) + "  ·  " + getString(PRICES[place.profile]), R.style.TextAppearance_CookPilot_Body);
        text(content, getString(R.string.cookmap_status) + "  ·  " + getString(STATUS[place.profile]), R.style.TextAppearance_CookPilot_Body);
        openSheet();
    }

    private void showListing() {
        if (map == null || state.places.isEmpty()) return;
        state.listing = true;
        LinearLayout content = binding.placeContent;
        content.removeAllViews();
        text(content, getString(R.string.cookmap_list_title), R.style.TextAppearance_CookPilot_TitleL);
        text(content, getString(R.string.cookmap_list_subtitle), R.style.TextAppearance_CookPilot_Body);
        for (ShoppingPlace place : state.places) {
            MaterialCardView card = new MaterialCardView(requireContext());
            card.setRadius(getResources().getDimension(R.dimen.cook_radius_2xxlarge));
            card.setCardBackgroundColor(color(R.color.cook_layer_floating));
            card.setStrokeColor(color(R.color.cook_primary_deep));
            card.setStrokeWidth(dp(1));
            LinearLayout body = new LinearLayout(requireContext());
            body.setOrientation(LinearLayout.VERTICAL);
            body.setPadding(dp(16), dp(8), dp(16), dp(16));
            card.addView(body);
            illustration(body, place.profile, 120);
            text(body, getString(NAMES[place.profile]), R.style.TextAppearance_CookPilot_TitleS);
            text(body, getString(CATEGORIES[place.profile]) + " · " + distance(place), R.style.TextAppearance_CookPilot_Body);
            text(body, getString(R.string.cookmap_price) + " · " + getString(PRICES[place.profile]), R.style.TextAppearance_CookPilot_Label);
            chips(body, place.profile, 2);
            card.setOnClickListener(v -> selectPlace(place));
            card.setFocusable(true);
            card.setContentDescription(getString(NAMES[place.profile]) + ", " + getString(CATEGORIES[place.profile]) + ", " + distance(place));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = dp(12);
            content.addView(card, params);
        }
        openSheet();
    }

    private void openSheet() {
        binding.placeSheet.setVisibility(View.VISIBLE);
        binding.placeScroll.scrollTo(0, 0);
        sheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        binding.mapActions.setVisibility(View.GONE);
    }

    private void closeListing() {
        state.listing = false;
        if (state.selected >= 0) showDetails(findPlace(state.selected));
        else dismissSheet();
    }

    private void dismissSheet() {
        state.listing = false;
        clearSelection();
        sheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        updatePlacesButton();
    }

    private void clearSelection() {
        state.selected = -1;
        for (ShoppingPlace place : state.places) {
            Marker marker = markers.get(place.profile);
            if (marker != null) marker.setIcon(markerIcon(getString(NAMES[place.profile]), place.profile, false));
        }
    }

    private void back() {
        if (state.listing) closeListing();
        else if (state.selected >= 0) dismissSheet();
        else ((CookListFragment) requireParentFragment()).closeMap();
    }

    private void updatePlacesButton() {
        binding.mapActions.setVisibility(mapLoaded && !mapFailed && state.latitude != null
                && !state.listing && state.selected < 0 ? View.VISIBLE : View.GONE);
    }

    private void updatePadding() {
        if (binding == null || map == null) return;
        int height = binding.getRoot().getHeight();
        int top = binding.mapHeader.getBottom() + dp(8);
        int bottom = sheet.getState() == BottomSheetBehavior.STATE_HIDDEN ? dp(80)
                : Math.max(0, height - binding.placeSheet.getTop());
        map.setPadding(dp(16), top, dp(16), Math.max(0, Math.min(bottom, height - top - dp(64))));
    }

    private void fitBounds(LatLngBounds bounds) {
        if (map == null || binding == null || binding.getRoot().getWidth() == 0) return;
        updatePadding();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, dp(24)));
    }

    private String distance(ShoppingPlace place) {
        return place.distanceMeters < 1000 ? getString(R.string.cookmap_meters, Math.round(place.distanceMeters))
                : getString(R.string.cookmap_kilometers, String.format(new Locale("es", "PE"), "%.1f", place.distanceMeters / 1000));
    }

    private void text(LinearLayout parent, String value, int appearance) {
        TextView view = new TextView(requireContext());
        view.setTextAppearance(appearance);
        view.setIncludeFontPadding(false);
        view.setLineSpacing(dp(2), 1);
        view.setText(value);
        view.setPadding(0, dp(8), 0, dp(8));
        parent.addView(view, new LinearLayout.LayoutParams(-1, -2));
    }

    private void illustration(LinearLayout parent, int profile, int height) {
        ImageView image = new ImageView(requireContext());
        image.setImageResource(ART[profile]);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(color(R.color.cook_dark_floating));
        background.setCornerRadius(getResources().getDimension(R.dimen.cook_radius_xlarge));
        image.setBackground(background);
        image.setClipToOutline(true);
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        parent.addView(image, new LinearLayout.LayoutParams(-1, dp(height)));
    }

    private void chips(LinearLayout parent, int profile, int count) {
        ChipGroup group = new ChipGroup(requireContext());
        group.setChipSpacingHorizontal(dp(8));
        group.setChipSpacingVertical(dp(4));
        String[] labels = getResources().getStringArray(CHIPS[profile]);
        for (int i = 0; i < count; i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(labels[i]);
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color(R.color.cook_primary_tint)));
            chip.setTextColor(color(R.color.cook_primary_soft));
            group.addView(chip);
        }
        parent.addView(group);
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private int color(int resource) { return ContextCompat.getColor(requireContext(), resource); }
    private void cancelLocation() {
        if (locationRequest != null) { locationRequest.cancel(); locationRequest = null; }
        locating = false;
    }

    @Override public void onDestroyView() {
        cancelLocation();
        mapGeneration++;
        binding.getRoot().removeCallbacks(mapTimeout);
        if (map != null) {
            map.setOnMapClickListener(null);
            map.setOnMarkerClickListener(null);
            map.setOnMapLoadedCallback(null);
        }
        map = null;
        markers.clear();
        binding = null;
        super.onDestroyView();
    }
}

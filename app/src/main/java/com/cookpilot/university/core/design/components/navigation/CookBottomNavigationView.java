package com.cookpilot.university.core.design.components.navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.cookpilot.university.R;
import com.cookpilot.university.core.design.icons.CookIcons;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.io.InputStream;

public final class CookBottomNavigationView extends FrameLayout {

    public enum Item {
        HOME,
        RECIPES,
        SHOPPING
    }

    public interface OnItemSelectedListener {
        void onItemSelected(@NonNull Item item);
    }

    private final MaterialCardView[] actionCards = new MaterialCardView[3];
    private final ImageView[] actionIcons = new ImageView[3];

    private final int[] outlineIcons = {
            CookIcons.home(),
            CookIcons.search(),
            CookIcons.shoppingList()
    };

    private final int[] selectedIcons = {
            CookIcons.homeFilled(),
            CookIcons.searchFilled(),
            CookIcons.shoppingListFilled()
    };

    private Item selectedItem = Item.HOME;
    @Nullable
    private OnItemSelectedListener itemSelectedListener;

    private final MaterialCardView avatarCard;
    private final ImageView avatarImage;

    public CookBottomNavigationView(Context context) {
        this(context, null);
    }

    public CookBottomNavigationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CookBottomNavigationView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(
                R.layout.view_cook_bottom_navigation,
                this,
                true
        );

        actionCards[0] = findViewById(R.id.navHome);
        actionCards[1] = findViewById(R.id.navRecipes);
        actionCards[2] = findViewById(R.id.navShopping);

        actionIcons[0] = findViewById(R.id.navHomeIcon);
        actionIcons[1] = findViewById(R.id.navRecipesIcon);
        actionIcons[2] = findViewById(R.id.navShoppingIcon);

        avatarCard = findViewById(R.id.cookPilotAvatarCard);
        avatarImage = findViewById(R.id.cookPilotAvatarImage);

        for (int index = 0; index < actionCards.length; index++) {
            final int itemIndex = index;
            actionCards[index].setOnClickListener(view ->
                    selectItem(Item.values()[itemIndex], true)
            );
        }

        loadCookPilotAvatar();
        selectItem(Item.HOME, false);
    }

    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
        itemSelectedListener = listener;
    }

    public void setOnAvatarClickListener(@Nullable OnClickListener listener) {
        avatarCard.setOnClickListener(listener);
    }

    public void setSelectedItem(@NonNull Item item) {
        selectItem(item, false);
    }

    @NonNull
    public Item getSelectedItem() {
        return selectedItem;
    }

    private void selectItem(@NonNull Item item, boolean notify) {
        if (selectedItem == item && notify) {
            return;
        }

        selectedItem = item;

        int primary = ContextCompat.getColor(getContext(), R.color.cook_primary);
        int activeSurface = ContextCompat.getColor(
                getContext(),
                R.color.cook_primary_tint_strong
        );
        int iconColor = ContextCompat.getColor(
                getContext(),
                R.color.cook_light_strong
        );
        int borderWidth = getResources().getDimensionPixelSize(
                R.dimen.cook_border_2
        );

        for (int index = 0; index < actionCards.length; index++) {
            boolean selected = Item.values()[index] == item;
            MaterialCardView card = actionCards[index];
            ImageView icon = actionIcons[index];

            card.setCardBackgroundColor(
                    selected ? activeSurface : Color.TRANSPARENT
            );
            card.setStrokeColor(
                    selected ? primary : Color.TRANSPARENT
            );
            card.setStrokeWidth(selected ? borderWidth : 0);
            icon.setImageResource(
                    selected ? selectedIcons[index] : outlineIcons[index]
            );
            icon.setColorFilter(iconColor);
            card.setContentDescription(
                    contentDescriptionFor(Item.values()[index])
            );
        }

        if (notify) {
            performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            if (itemSelectedListener != null) {
                itemSelectedListener.onItemSelected(item);
            }
        }
    }

    private String contentDescriptionFor(Item item) {
        switch (item) {
            case RECIPES:
                return getContext().getString(R.string.nav_search);
            case SHOPPING:
                return getContext().getString(R.string.nav_shopping);
            case HOME:
            default:
                return getContext().getString(R.string.nav_home);
        }
    }

    private void loadCookPilotAvatar() {
        try (InputStream stream = getContext()
                .getAssets()
                .open("images/cookpilot.png")) {
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            avatarImage.setImageBitmap(bitmap);
        } catch (IOException ignored) {
            avatarImage.setImageResource(CookIcons.user());
        }
    }
}
package com.cookpilot.university.core.design.icons;

import io.eyram.iconsax.BoldIcons;
import io.eyram.iconsax.LinearIcons;

public final class CookIcons {

    private CookIcons() {
    }

    public static int home() {
        return LinearIcons.INSTANCE.getHome2();
    }

    public static int homeFilled() {
        return BoldIcons.INSTANCE.getHome1();
    }

    public static int search() {
        return LinearIcons.INSTANCE.getSearchNormal1();
    }

    public static int shoppingList() {
        return LinearIcons.INSTANCE.getShoppingBag();
    }

    public static int shoppingListFilled() {
        return BoldIcons.INSTANCE.getShoppingBag();
    }

    public static int breakfast() {
        return LinearIcons.INSTANCE.getSun1();
    }

    public static int lunch() {
        return LinearIcons.INSTANCE.getSunFog();
    }

    public static int dinner() {
        return BoldIcons.INSTANCE.getMoon();
    }

    public static int user() {
        return LinearIcons.INSTANCE.getUser();
    }

    public static int ingredients() {
        return LinearIcons.INSTANCE.getMenuBoard();
    }

    public static int add() {
        return LinearIcons.INSTANCE.getAdd();
    }

    public static int more() {
        return LinearIcons.INSTANCE.getMore2();
    }

    public static int selected() {
        return LinearIcons.INSTANCE.getTickCircle();
    }

    public static int eye() {
        return LinearIcons.INSTANCE.getEye();
    }

    public static int eyeSlash() {
        return LinearIcons.INSTANCE.getEyeSlash();
    }

    public static int closeCircle() {
        return LinearIcons.INSTANCE.getCloseCircle();
    }
}

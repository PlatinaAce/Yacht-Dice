//역할: 주사위 동작 구현.

import java.util.Random;

class Dice {
    private int value;

    public Dice() {
        value = 0;
    }

    public void roll() {
        value = (int) (Math.random() * 6) + 1;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}


package com.training.apparatus.keyboards;

import lombok.Data;

@Data
public class Command {
    boolean shift; Hand hand; Finger finger; Position[] position;
    public Command(boolean shift, Hand hand, Finger finger, Position... position) {
        this.shift = shift;
        this.hand = hand;
        this.finger = finger;
        this.position = position;
    }
}

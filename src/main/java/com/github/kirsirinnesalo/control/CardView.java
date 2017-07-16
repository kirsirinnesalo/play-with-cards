package com.github.kirsirinnesalo.control;

import com.github.kirsirinnesalo.model.Card;
import com.github.kirsirinnesalo.scene.util.Utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static com.github.kirsirinnesalo.model.Card.Rank.JOKER;

public class CardView extends ImageView {

    private final Card card;
    private final Image face;
    private final Image back;
    private boolean faceUp = false;

    public CardView(Card card) {
        this.card = card;
        face = createFaceImage();
        back = createBackImage();
        setImage(back);
    }

    public CardView(Card card, double cardWidth, double cardHeight) {
        this(card);
        setFitWidth(cardWidth);
        setFitHeight(cardHeight);
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void turnFaceDown() {
        faceUp = false;
        setImage(back);
    }

    public void turnFaceUp() {
        faceUp = true;
        setImage(face);
    }

    public void turnFace() {
        if (isFaceUp()) {
            turnFaceDown();
        } else {
            turnFaceUp();
        }
    }

    private Image createFaceImage() {
        if (JOKER.equals(card.rank)) {
            Card.Color color = card.getColor();
            return loadCardImage(color.name().toLowerCase() + "_" + "joker");
        }
        return loadCardImage(card.rank.asText() + "_of_" + card.suit.asText());
    }

    private Image createBackImage() {
        return loadCardImage("back");
    }

    private Image loadCardImage(String cardName) {
        String imagePath = "cards/" + cardName + ".png";
        return Utils.getImage(imagePath);
    }

    public Card getCard() {
        return card;
    }
}

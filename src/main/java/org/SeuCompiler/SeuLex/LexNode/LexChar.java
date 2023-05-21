package org.SeuCompiler.SeuLex.LexNode;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.SeuCompiler.SeuLex.LexNode.SpecialChar;

import java.util.Objects;

@Getter
@EqualsAndHashCode
public final class LexChar {
    private final Boolean isSpecial;
    private final SpecialChar spChar;
    private final Character character;

    public LexChar(Character character) {
        this.isSpecial = false;
        this.spChar = null;
        this.character = character;
    }

    public LexChar(SpecialChar spChar) {
        this.isSpecial = true;
        this.spChar = spChar;
        this.character = null;
    }

    public boolean equalsTo(Character character) {
        if (this.isSpecial) return false;
        else return Objects.equals(this.character, character);
    }

    public boolean equalsTo(SpecialChar spChar) {
        if (!this.isSpecial) return false;
        else return this.spChar == spChar;
    }

    public String getString() {
        if(isSpecial && this.spChar!=null)
            return this.spChar.getStr();
        else if (this.character != null) {
            return this.character.toString();
        }
        else
            return "null";
    }
}

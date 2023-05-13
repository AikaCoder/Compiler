package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode
final class FAChar {
    private final Boolean isSpecial;
    private final SpecialChar spChar;
    private final Character character;

    public FAChar(Character character) {
        this.isSpecial = false;
        this.spChar = null;
        this.character = character;
    }

    public FAChar(SpecialChar spChar) {
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
}

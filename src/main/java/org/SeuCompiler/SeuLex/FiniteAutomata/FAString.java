package org.SeuCompiler.SeuLex.FiniteAutomata;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode
public final class FAString {
    private final Boolean isSpecial;
    private final SpecialChar spChar;
    private final String string;

    public FAString(String str) {
        this.isSpecial = false;
        this.spChar = null;
        this.string = str;
    }

    public FAString(SpecialChar spChar) {
        this.isSpecial = true;
        this.spChar = spChar;
        this.string = null;
    }

    public boolean equalsToStr(String str) {
        if (this.isSpecial) return false;
        else return Objects.equals(this.string, str);
    }

    public boolean equalsToSpChar(SpecialChar spChar) {
        if (!this.isSpecial) return false;
        else return this.spChar == spChar;
    }
}

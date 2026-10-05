package co.cngn.sdk.wallet;

import org.junit.jupiter.api.Test;
import static co.cngn.sdk.wallet.Wallets.Network.*;
import static org.junit.jupiter.api.Assertions.*;

class WalletsTest {
    private static final String MNEMONIC = "abandon abandon abandon abandon abandon abandon "
            + "abandon abandon abandon abandon abandon about";

    @Test void derivesKnownWalletAddresses() {
        var eth = Wallets.fromMnemonic(ETHEREUM, MNEMONIC);
        var base = Wallets.fromMnemonic(BASE, MNEMONIC);
        assertEquals("0x9858EfFD232B4033E47d90003D41EC34EcaEda94", eth.address());
        assertEquals(eth.address(), base.address());
        assertTrue(Wallets.validateAddress(BASE, eth.address()));
        assertFalse(Wallets.validateAddress(BASE, "0x9858effd232B4033E47d90003D41EC34EcaEda94"));
        assertFalse(eth.toString().contains(eth.privateKey()));
        assertFalse(eth.toString().contains(MNEMONIC));

        var tron = Wallets.fromMnemonic(TRON, MNEMONIC);
        assertEquals("TUEZSdKsoDHQMeZwihtdoBiN46zxhGWYdH", tron.address());
        assertTrue(Wallets.validateAddress(TRON, tron.address()));
        assertFalse(Wallets.validateAddress(TRON, tron.address().substring(0, 33) + "A"));

        var stellar = Wallets.fromMnemonic(STELLAR, MNEMONIC);
        assertEquals("GB3JDWCQJCWMJ3IILWIGDTQJJC5567PGVEVXSCVPEQOTDN64VJBDQBYX", stellar.address());
        assertTrue(Wallets.validateAddress(STELLAR, stellar.address()));
        assertFalse(Wallets.validateAddress(STELLAR, "GINVALID"));

        var solana = Wallets.fromMnemonic(SOLANA, MNEMONIC);
        assertEquals("HAgk14JpMQLgt6rVgv7cBQFJWFto5Dqxi472uT3DKpqk", solana.address());
        assertTrue(Wallets.validateAddress(SOLANA, solana.address()));
        assertFalse(Wallets.validateAddress(SOLANA, "bad"));

        var xbn = Wallets.fromMnemonic(XBN, MNEMONIC);
        assertEquals("GD7OOH7FGE25NJDQ67X23AB5IG6UGGOFV6HKOB6AKWTXFAXR262ZEAVB", xbn.address());
        assertTrue(Wallets.validateAddress(XBN, xbn.address()));
        assertFalse(Wallets.validateAddress(XBN, "bad"));
    }

    @Test void generatesRestorableWalletAndRejectsBadPhrase() {
        var created = Wallets.generate(BSC);
        assertEquals(12, created.mnemonic().split(" ").length);
        assertEquals(created.address(), Wallets.fromMnemonic(BSC, created.mnemonic()).address());
        assertThrows(IllegalArgumentException.class, () -> Wallets.fromMnemonic(BASE, "abandon abandon"));
    }
}

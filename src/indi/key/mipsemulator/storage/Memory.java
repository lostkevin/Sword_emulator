package indi.key.mipsemulator.storage;

import indi.key.mipsemulator.model.interfaces.Resetable;
import indi.key.mipsemulator.model.exception.MemoryOutOfBoundsException;

public interface Memory extends Resetable {
    void save(long address, byte[] bytes) throws MemoryOutOfBoundsException;

    byte[] load(long address, int bytesNum) throws MemoryOutOfBoundsException;

    // This method ensures that the storage data will not be changed.
    byte[] loadConstantly(long address, int bytesNum) throws MemoryOutOfBoundsException;
}

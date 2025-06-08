package net.rizecookey.racc0on.assembler;

public interface Assembler {
    byte[] assemble(String assemblyInput) throws AssemblerException;
}

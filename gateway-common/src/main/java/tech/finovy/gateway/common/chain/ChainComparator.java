package tech.finovy.gateway.common.chain;

import java.util.Comparator;

public class ChainComparator implements Comparator<ChainListener> {
    @Override
    public int compare(ChainListener first, ChainListener sen) {
        return Integer.compare(first.getOrder(), sen.getOrder());
    }
}

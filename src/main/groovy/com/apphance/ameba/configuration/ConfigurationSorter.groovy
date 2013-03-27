package com.apphance.ameba.configuration

class ConfigurationSorter {

    private List<Configuration> configurations = []

    void addAll(Collection<Configuration> amebaConfigurations) {
        configurations.addAll(amebaConfigurations)
    }

    List<Configuration> sort() {
        configurations.sort { a, b -> a.order.compareTo(b.order) }
    }
}

package com.apphance.ameba.executor.stream

import spock.lang.Specification

class AppendableAggregateSpec extends Specification {

    def 'call empty aggregate with no expectations'() {
        given:
        def appendableAggreate = new AppendableAggregate([])

        when:
        appendableAggreate.append('c')

        then:
        noExceptionThrown()
    }

    def 'call aggregate with several targets'() {
        given:
        def appendables = (1..5).collect{Mock(Appendable)}

        and:
        def aggregate = new AppendableAggregate(appendables)

        when:
        aggregate.append('a' as Character)
        aggregate.append('b')
        aggregate.append('cd', 0, 1)

        then:
        interaction {
            appendables.each {
                1 * it.append('a' as Character)
            }
        }

        then:
        interaction {
            appendables.each {
                1 * it.append('b')
            }
        }

        then:
        interaction {
            appendables.each {
                1 * it.append('cd', 0, 1)
            }
        }
    }
}

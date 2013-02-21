package com.apphance.ameba.executor.stream

import spock.lang.Specification
import spock.lang.Unroll

class AppendableAdapterSpec extends Specification {

    @Unroll
    def 'passes string to closure when called #method for #args'() {
        given:
        def c = Mock(StringAppendable)

        and:
        Appendable appendableAdapter = new AppendableAdapter(c)

        when:
        def returnedAppendable = appendableAdapter."$method"(args)

        then:
        1 * c.append(string)

        then:
        0 * c.append(_)

        and: 'returns self'
        returnedAppendable == appendableAdapter

        where:
        method   | args                                  | string
        'append' | 'c' as Character                      | 'c'
        'append' | 'cs' as CharSequence                  | 'cs'
        'append' | ['cs2' as CharSequence, 0, 2]         | 'cs'
        'append' | ['long_string' as CharSequence, 0, 2] | 'lo'
    }
}

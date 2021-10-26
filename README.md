# Are you using ARCADE? Please let us know!

We'd love to know more about the project you use ARCADE for. This information allows us to apply for more funding that can support future development and maintenance of ARCADE. Feel free to contact either [Nenad Medvidović](mailto:neno@usc.edu), [Joshua Garcia](mailto:joshug4@uci.edu) or [Marcelo Schmitt Laser](mailto:schmittl@usc.edu) to let us know more about how you use ARCADE.

# What is ARCADE?

Architecture Recovery, Change, And Decay Evaluator (ARCADE) is a software workbench that employs (1) a suite of architecture-recovery techniques, (2) a catalogue of architectural smell definitions, (3) accompanying smell-detection algorithms, and (4) a set of metrics for measuring different aspects of architectural change and decay. ARCADE combines these elements to investigate a variety of questions regarding architectural change and decay.

ARCADE’s foundational element is its suite of architecture-recovery techniques. The architectures produced by the Recovery Techniques are used for studying change and decay. ARCADE currently provides access to eight recovery techniques. This allows an engineer to (1) extract multiple architectural views and to (2) ensure maximum accuracy of extracted architectures by highlighting their different aspects.

# ARCADE Core

ARCADE Core is a fork of [ARCADE](https://bitbucket.org/joshuaga/arcade) containing all of its primary functionalities, updated with modern programming practices for ease of use. ARCADE Core uses a minimal set of external dependencies and is packaged using Maven and Java 11. All functions described in the [manual](https://tiny.cc/arcademanual) are present and have been thoroughly tested and debugged to ensure correctness. We welcome any suggestions and bug reports you may have, which can be posted in the form of Issues in this repository. You may also e-mail the primary ARCADE Core maintainer, [Marcelo Schmitt Laser](mailto:schmittl@usc.edu), for any questions or concerns regarding how to use ARCADE Core.

# Contributors

[**Prof. Joshua Garcia**](https://jgarcia.ics.uci.edu/) is the original creator of ARCADE, and author of the ARC clustering technique. He also co-designed and implemented ARCADE's architectural decay prediction tools.

[**Prof. Nenad Medvidović**](https://viterbi.usc.edu/directory/faculty/Medvidovic/Nenad) is the head of the [Software Architecture Research Group](https://softarch.usc.edu/), where ARCADE was created and is maintained.

[**Dr. Duc Minh Le**](https://lemduc.github.io/) is a major contributor to ARCADE, and is responsible for much of its smell detection components.

**Marcelo Schmitt Laser** is the current maintainer of ARCADE, and creator of ARCADE Core.

[**Daye Nam**](https://dayenam.com/) is the creator of [EVA](https://github.com/namdy0429/EVA), a visualization tool that utilizes ARCADE's results to create graphical presentations of architectural information.

[**Dr. Arman Shahbazian**](http://shahbazian.me/) created several extensions to ARCADE for the recovery of architectural design decision information.

[**Dr. Ehsan Kouroshfar**](https://www.linkedin.com/in/ekouroshfar/) and [**Prof. Sam Malek**](https://malek.ics.uci.edu/) co-designed and implemented architectural decay prediction tools based on ARCADE.

[**Prof. Igor Ivkovic**](https://uwaterloo.ca/systems-design-engineering/profile/iivkovic) is a co-author of the first-ever publication involving ARCADE, and contributed significantly to its original vision.

[**Sylvia Wong**](https://www.linkedin.com/in/sylvia-wong) contributed towards the modernization effort in ARCADE Core, being largely responsible for its CI and test suite.

[**Gina Yang**](https://www.linkedin.com/in/gina-yang861) developed a large part of the ARCADE Core test suite and was responsible for generating the majority of its test oracles.

**Khoi Pham** developed much of the infrastructure that enabled the integration of ARCADE into SAIN, the Software Architecture Instrument.
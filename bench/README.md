## Benchmarks

[results.ods](results.ods) contains measurements of running
[ipae2e.sc](../src/ipae2e.sc).
This readme documents the contents of the spreadsheet and the
benchmarking environment.

### Inputs

Two kinds of generated inputs were used:
 1. [The private-attribution/research-prototype project's input generation script](https://github.com/private-attribution/research-prototype/blob/019475faabfc6c57b91587bbaedb8ed6e59c25a9/ipa/generate_input.py)
    was used to create inputs for the 32-bit match key runs. These files
    were truncated to the first 100k/500k/1M rows, preprocessed with
    [mp-spdz-input-to-csv.py](../scripts/mp-spdz-input-to-csv.py), and
    uploaded to the cluster using [this schema](../inputs/mpspdz/mp-spdz-schema.xml).
 1. [The `gen_events` target of the private-attribution/ipa project](https://github.com/private-attribution/ipa/blob/4aa7d84cfa58fc1c3082e08408e0cd67a4232328/src/bin/ipa_bench/gen_events.rs)
    generated inputs for the 64-bit match key runs and multi-column
    sorting runs (match key & timestamp). Scale factors 1, 5, and 10
    were used as arguments for different size tests. The resulting files
    were converted to CSVs with [rust-input-to-csv.py](../scripts/rust-input-to-csv.py)
    and imported using [this schema](../inputs/rust/rust-schema.xml).

### Environment

Benchmarks were run locally with all three parties in the same virtual
machine. An artificial 10ms delay was introduced for the loopback
interface to simulate a 20ms RTT between computing parties:

`tc qdisc add dev lo:1 root netem delay 10ms`

Bandwidth was kept unlimited, however usage averaged at around 500
Mbit/s during the sorting and aggregation phases with peaks up to 750
Mbit/s.

### Metrics

[results.ods](results.ods) has three sheets corresponding to the inputs
and configuration used:
  - __32-bit keys__ has measurements of the first kind of generated
    inputs with 32-bit match keys.
  - __64-bit keys__ used the second kind of generated inputs with 64-bit
    match keys.
  - __64-bit with timestamp sorting__ used the latter inputs along with
    the initial sorting done on both the match key and timestamp.

For each stage of IPA, and for each size of input, wall-clock time and
transferred bytes of a single computation node is shown.
For comparing cost of running in the cloud, the same cost calculation
is shown as in the [PATCG benchmark slides](https://raw.githubusercontent.com/patcg/meetings/main/2022/08/09-telecon/IPA-August-2022-Update-PATCG-Issue-70.pdf).




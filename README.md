# Interoperable Private Attribution (IPA) implemenation based on the SecreC Analytics Library

This implementation is based on the [Private Advertising Technology Community Group's IPA private measurement proposal](https://github.com/patcg-individual-drafts/ipa) with three
party MPC and a passive (honest, but curious) adversary.

## [src/ipae2e.sc](src/ipae2e.sc)

The implementation follows the four stages as described in the proposal
  - __Sorting__ uses the Analytics Library based table sort.
  - __Attribution__ follows the vectorized Oblivious Last Touch Attribution method described [here](https://github.com/patcg-individual-drafts/ipa/blob/main/IPA-End-to-End.md#oblivious-last-touch-attribution).
  - __Capping__ is the same as in the MP-SPDZ research prototype, used
    for the PATCG benchmarks:
    [source](https://github.com/bmcase/raw-ipa/blob/08c8b53b57ce517e145029959d8b9a3e21458f11/research-prototype/Programs/Source/vectorized.mpc#L203).
  - __Aggregation__ relies on the Analytics Library Group By
    functionality.

## Benchmarking

Benchmarking results and documentation can be found in [bench/](bench).


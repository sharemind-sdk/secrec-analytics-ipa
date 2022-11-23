#!/usr/bin/python3

'''
This file is a part of the Sharemind framework.
Copyright (C) Cybernetica AS

All rights are reserved. Reproduction in whole or part is prohibited
without the written consent of the copyright owner. The usage of this
code is subject to the appropriate license agreement.

----

This takes the generated input from the `private-attribution/ipa`
(i.e. Rust) implementation of IPA and turns it into a valid CSV for
`sharemind-csv-importer`.
The Rust events are in a JSON format.

Running:
    `./rust-input-to-csv.py <input file>`
'''

import json


def main(filename: str):
    out_filename = f"{filename}.csv"
    with open(out_filename, "w") as of:
        of.write("match_key timestamp is_trigger value breakdown_key\n")  # Using space as delimiter
        with open(filename, "r") as f:
            max_match = 0
            for line in f:
                o : dict = json.loads(line.strip())
                assert "Trigger" in o.keys() or "Source" in o.keys()
                inner = next(iter(o))  # extract type
                max_match = max(max_match, int(o[inner]['event']['matchkey']))
                of.write(f"{o[inner]['event']['matchkey']} {o[inner]['event']['timestamp']} ")
                if inner == "Trigger":
                    of.write(f"1 {o['Trigger']['value']} 0\n")
                else:
                    of.write(f"0 0 {o['Source']['breakdown_key']}\n")
            print(f"maximum match key = {max_match}")


if __name__ == "__main__":
    import sys
    main(sys.argv[1])

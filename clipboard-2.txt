from bcc import BPF
from time import sleep

# Define the eBPF program
bpf_prog = """
#include <uapi/linux/ptrace.h>

struct key_t {
    u32 pid;
    u64 method_id;
};

BPF_HASH(start, struct key_t);
BPF_HISTOGRAM(dist);

int trace_method_enter(struct pt_regs *ctx, u32 pid, u64 method_id) {
    u64 ts = bpf_ktime_get_ns();

    struct key_t key = {};
    key.pid = pid;
    key.method_id = method_id;

    start.update(&key, &ts);
    return 0;
}

int trace_method_exit(struct pt_regs *ctx, u32 pid, u64 method_id) {
    struct key_t key = {};
    key.pid = pid;
    key.method_id = method_id;

    u64 *tsp = start.lookup(&key);
    if (tsp == 0) {
        return 0;
    }

    u64 delta = bpf_ktime_get_ns() - *tsp;
    dist.increment(bpf_log2l(delta));

    start.delete(&key);
    return 0;
}
"""

# Attach eBPF program to Java methods
bpf = BPF(text=bpf_prog)
bpf.attach_uprobe(
    name="libjvm.so",
    sym="Method::invoke",
    fn_name="trace_method_enter",
    pid=-1,
    tgid=-1,
)
bpf.attach_uretprobe(
    name="libjvm.so",
    sym="Method::invoke",
    fn_name="trace_method_exit",
    pid=-1,
    tgid=-1,
)

# Print histogram
print("Tracing... Ctrl-C to end.")
try:
    sleep(99999999)
except KeyboardInterrupt:
    print("\nHistogram of method execution times (in microseconds):")
    bpf["dist"].print_log2_hist("microseconds")

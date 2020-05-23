#include <jni.h>
#include <xmmintrin.h>
#include <immintrin.h>
#include <math.h>
#include <stdint.h>
#include <stdlib.h>

JNIEXPORT jlong JNICALL Java_bench_Matrix4fn_allocate(JNIEnv* env, jclass clazz) {
    void* ptr;
    if (posix_memalign(&ptr, 32, 16 << 2) == 0)
        return (jlong)(intptr_t)ptr;
    return 0L;
}
JNIEXPORT void JNICALL Java_bench_Matrix4fn_free(JNIEnv* env, jclass clazz, jlong mem) {
    free((void*)(intptr_t)mem);
}

/*
 * https://stackoverflow.com/questions/18499971/efficient-4x4-matrix-multiplication-c-vs-assembly#answer-18508113
 */
inline static void mulSSE(const float* a, const float* b, float* r) {
	__m128 col1 = _mm_load_ps(&a[0]);
	__m128 col2 = _mm_load_ps(&a[4]);
	__m128 col3 = _mm_load_ps(&a[8]);
	__m128 col4 = _mm_load_ps(&a[12]);
	for (int i = 0; i < 4; i++) {
		__m128 brod1 = _mm_set1_ps(b[i * 4 + 0]);
		__m128 brod2 = _mm_set1_ps(b[i * 4 + 1]);
		__m128 brod3 = _mm_set1_ps(b[i * 4 + 2]);
		__m128 brod4 = _mm_set1_ps(b[i * 4 + 3]);
		__m128 col = _mm_add_ps(
			_mm_add_ps(
				_mm_mul_ps(brod1, col1),
				_mm_mul_ps(brod2, col2)),
			_mm_add_ps(
				_mm_mul_ps(brod3, col3),
				_mm_mul_ps(brod4, col4)));
		_mm_store_ps(&r[i * 4], col);
	}
}

/*
 * https://stackoverflow.com/questions/19806222/matrix-vector-multiplication-in-avx-not-proportionately-faster-than-in-sse#answer-46058667
 */
typedef struct MATRIX__ {
    union {
        float  f[4][4];
        __m128 m[4];
        __m256 n[2];
    };
} MATRIX;
inline static void mulAVX(const MATRIX* M1, const MATRIX* M2, MATRIX* MD) {
    __m256 a0, a1, b0, b1;
    __m256 c0, c1, c2, c3, c4, c5, c6, c7;
    __m256 t0, t1, u0, u1;

    t0 = M1->n[0];                                                   // t0 = a00, a01, a02, a03, a10, a11, a12, a13
    t1 = M1->n[1];                                                   // t1 = a20, a21, a22, a23, a30, a31, a32, a33
    u0 = M2->n[0];                                                   // u0 = b00, b01, b02, b03, b10, b11, b12, b13
    u1 = M2->n[1];                                                   // u1 = b20, b21, b22, b23, b30, b31, b32, b33

    a0 = _mm256_shuffle_ps(t0, t0, _MM_SHUFFLE(0, 0, 0, 0));        // a0 = a00, a00, a00, a00, a10, a10, a10, a10
    a1 = _mm256_shuffle_ps(t1, t1, _MM_SHUFFLE(0, 0, 0, 0));        // a1 = a20, a20, a20, a20, a30, a30, a30, a30
    b0 = _mm256_permute2f128_ps(u0, u0, 0x00);                      // b0 = b00, b01, b02, b03, b00, b01, b02, b03
    c0 = _mm256_mul_ps(a0, b0);                                     // c0 = a00*b00  a00*b01  a00*b02  a00*b03  a10*b00  a10*b01  a10*b02  a10*b03
    c1 = _mm256_mul_ps(a1, b0);                                     // c1 = a20*b00  a20*b01  a20*b02  a20*b03  a30*b00  a30*b01  a30*b02  a30*b03

    a0 = _mm256_shuffle_ps(t0, t0, _MM_SHUFFLE(1, 1, 1, 1));        // a0 = a01, a01, a01, a01, a11, a11, a11, a11
    a1 = _mm256_shuffle_ps(t1, t1, _MM_SHUFFLE(1, 1, 1, 1));        // a1 = a21, a21, a21, a21, a31, a31, a31, a31
    b0 = _mm256_permute2f128_ps(u0, u0, 0x11);                      // b0 = b10, b11, b12, b13, b10, b11, b12, b13
    c2 = _mm256_mul_ps(a0, b0);                                     // c2 = a01*b10  a01*b11  a01*b12  a01*b13  a11*b10  a11*b11  a11*b12  a11*b13
    c3 = _mm256_mul_ps(a1, b0);                                     // c3 = a21*b10  a21*b11  a21*b12  a21*b13  a31*b10  a31*b11  a31*b12  a31*b13

    a0 = _mm256_shuffle_ps(t0, t0, _MM_SHUFFLE(2, 2, 2, 2));        // a0 = a02, a02, a02, a02, a12, a12, a12, a12
    a1 = _mm256_shuffle_ps(t1, t1, _MM_SHUFFLE(2, 2, 2, 2));        // a1 = a22, a22, a22, a22, a32, a32, a32, a32
    b1 = _mm256_permute2f128_ps(u1, u1, 0x00);                      // b0 = b20, b21, b22, b23, b20, b21, b22, b23
    c4 = _mm256_mul_ps(a0, b1);                                     // c4 = a02*b20  a02*b21  a02*b22  a02*b23  a12*b20  a12*b21  a12*b22  a12*b23
    c5 = _mm256_mul_ps(a1, b1);                                     // c5 = a22*b20  a22*b21  a22*b22  a22*b23  a32*b20  a32*b21  a32*b22  a32*b23

    a0 = _mm256_shuffle_ps(t0, t0, _MM_SHUFFLE(3, 3, 3, 3));        // a0 = a03, a03, a03, a03, a13, a13, a13, a13
    a1 = _mm256_shuffle_ps(t1, t1, _MM_SHUFFLE(3, 3, 3, 3));        // a1 = a23, a23, a23, a23, a33, a33, a33, a33
    b1 = _mm256_permute2f128_ps(u1, u1, 0x11);                      // b0 = b30, b31, b32, b33, b30, b31, b32, b33
    c6 = _mm256_mul_ps(a0, b1);                                     // c6 = a03*b30  a03*b31  a03*b32  a03*b33  a13*b30  a13*b31  a13*b32  a13*b33
    c7 = _mm256_mul_ps(a1, b1);                                     // c7 = a23*b30  a23*b31  a23*b32  a23*b33  a33*b30  a33*b31  a33*b32  a33*b33

    c0 = _mm256_add_ps(c0, c2);                                     // c0 = c0 + c2 (two terms, first two rows)
    c4 = _mm256_add_ps(c4, c6);                                     // c4 = c4 + c6 (the other two terms, first two rows)
    c1 = _mm256_add_ps(c1, c3);                                     // c1 = c1 + c3 (two terms, second two rows)
    c5 = _mm256_add_ps(c5, c7);                                     // c5 = c5 + c7 (the other two terms, second two rose)

    // Finally complete addition of all four terms and return the results
    MD->n[0] = _mm256_add_ps(c0, c4);       // n0 = a00*b00+a01*b10+a02*b20+a03*b30  a00*b01+a01*b11+a02*b21+a03*b31  a00*b02+a01*b12+a02*b22+a03*b32  a00*b03+a01*b13+a02*b23+a03*b33
                                                //      a10*b00+a11*b10+a12*b20+a13*b30  a10*b01+a11*b11+a12*b21+a13*b31  a10*b02+a11*b12+a12*b22+a13*b32  a10*b03+a11*b13+a12*b23+a13*b33
    MD->n[1] = _mm256_add_ps(c1, c5);       // n1 = a20*b00+a21*b10+a22*b20+a23*b30  a20*b01+a21*b11+a22*b21+a23*b31  a20*b02+a21*b12+a22*b22+a23*b32  a20*b03+a21*b13+a22*b23+a23*b33
                                                //      a30*b00+a31*b10+a32*b20+a33*b30  a30*b01+a31*b11+a32*b21+a33*b31  a30*b02+a31*b12+a32*b22+a33*b32  a30*b03+a31*b13+a32*b23+a33*b33
}

JNIEXPORT void JNICALL Java_bench_Matrix4fn_noop(JNIEnv* env, jclass clazz, jlong m0, jlong m1, jlong dest) {}
JNIEXPORT void JNICALL JavaCritical_bench_Matrix4fn_noop(jlong m0, jlong m1, jlong dest) {}

JNIEXPORT void JNICALL Java_bench_Matrix4fn_mulSSE(JNIEnv* env, jclass clazz, jlong m0, jlong m1, jlong dest) {
	mulSSE((const float*)(intptr_t)m0, (const float*)(intptr_t)m1, (float*)(intptr_t)dest);
}
JNIEXPORT void JNICALL JavaCritical_bench_Matrix4fn_mulSSE(jlong m0, jlong m1, jlong dest) {
	mulSSE((const float*)(intptr_t)m0, (const float*)(intptr_t)m1, (float*)(intptr_t)dest);
}

JNIEXPORT void JNICALL Java_bench_Matrix4fn_mulAVX(JNIEnv* env, jclass clazz, jlong m0, jlong m1, jlong dest) {
	mulAVX((const MATRIX*)(intptr_t)m0, (const MATRIX*)(intptr_t)m1, (MATRIX*)(intptr_t)dest);
}
JNIEXPORT void JNICALL JavaCritical_bench_Matrix4fn_mulAVX(jlong m0, jlong m1, jlong dest) {
	mulAVX((const MATRIX*)(intptr_t)m0, (const MATRIX*)(intptr_t)m1, (MATRIX*)(intptr_t)dest);
}

#include <jni.h>
#include <xmmintrin.h>
#include <math.h>
#include <stdint.h>
#include <stdlib.h>

JNIEXPORT jlong JNICALL Java_bench_Matrix4fn_allocate(JNIEnv* env, jclass clazz) {
    void* ptr;
    if (posix_memalign(&ptr, 16, 16 << 2) == 0)
        return (jlong)(intptr_t)ptr;
    return 0L;
}
JNIEXPORT void JNICALL Java_bench_Matrix4fn_free(JNIEnv* env, jclass clazz, jlong mem) {
    free((void*)(intptr_t)mem);
}

/*
 * https://stackoverflow.com/questions/18499971/efficient-4x4-matrix-multiplication-c-vs-assembly#answer-18508113
 */
inline static void mulNative(jlong m0, jlong m1, jlong dest) {
	const float* a = (const float*)(intptr_t)m0;
	const float* b = (const float*)(intptr_t)m1;
	float* r = (float*)(intptr_t)dest;
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

JNIEXPORT void JNICALL Java_bench_Matrix4fn_mul(JNIEnv* env, jclass clazz, jlong m0, jlong m1, jlong dest) {
	mulNative(m0, m1, dest);
}
JNIEXPORT void JNICALL JavaCritical_bench_Matrix4fn_mul(jlong m0, jlong m1, jlong dest) {
	mulNative(m0, m1, dest);
}

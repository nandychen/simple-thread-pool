����Executor�ύ����������ʱ������ϣ����������ɺ��ý���������FutureTask��
�����ѭ����ȡtask������future.get()ȥ��ȡ���������������taskû����ɣ���͵����������
���ʵЧ�Բ��ߣ���ʵ�ںܶೡ�ϣ���ʵ���õ�һ��������ʱ����ʱ�����û�����ɲ���������ʵ�������ڵ�һ������ʱ��
�ڶ���task�������Ѿ��������ˣ���Ȼ���������future task�����ʵģ�Ч��Ҳ���ߡ�


* �Լ�ά��list��CompletionService������

1. ��list�б�����ÿ��Future���󲢲�һ���������״̬����ʱ����get()�����ͻᱻ����ס��
���ϵͳ����Ƴ�ÿ���߳���ɺ���ܸ�������������������£�
�������ڴ���list����ĵ�������ɵ��߳̾ͻ������˶���ĵȴ�ʱ�䡣
2. ��CompletionService��ʵ����ά��һ������Future�����BlockingQueue��
ֻ�е����Future����״̬�ǽ�����ʱ�򣬲Ż���뵽���Queue�У�take()������ʵ����Producer-Consumer�е�Consumer��
�����Queue��ȡ��Future�������Queue�ǿյģ��ͻ����������ֱ������ɵ�Future������뵽Queue�С�

* CompletionService��ȡ����BlockingQueue<Future<V>>�޽����������Future��
����һ���߳�ִ����ϰѷ��ؽ���ŵ�BlockingQueue<Future<V>>���档�Ϳ���ͨ��completionServcie.take().get()ȡ�������

* ��������

1. take ����ȡ���Ƴ���ʾ��һ������������ Future�����Ŀǰ������������������ȴ���<�����Ҫ�õ�����ֵ������take>
2. poll ��ȡ���Ƴ���ʾ��һ������������ Future����������������������򷵻�null��


* ������jdk����CompletionService�ļ�飺

1. public interface CompletionService<V>
2. �������µ��첽������ʹ�����������Ľ�����뿪���ķ��������� submit ִ�е�����
ʹ���� take ����ɵ����񣬲����������Щ�����˳�������ǵĽ����
���磬CompletionService �������������첽 IO ��ִ�ж�������������Ϊ�����ϵͳ��һ�����ύ��
Ȼ�󣬵���ɶ�����ʱ�����ڳ���Ĳ�ͬ����ִ������������ִ�в�����˳��������������˳��ͬ��
3. ͨ����CompletionService ������һ�������� Executor ��ʵ��ִ������
����������£�CompletionService ֻ����һ���ڲ���ɶ��С�ExecutorCompletionService ���ṩ�˴˷�����һ��ʵ�֡�
4. �ڴ�һ����Ч�����߳����� CompletionService �ύ����֮ǰ�Ĳ��� happen-before ������ִ�еĲ�����
�������� happen-before �����ڴӶ�Ӧ take() �ɹ����صĲ�����

* �ο��ĵ�
http://www.tuicool.com/articles/umyy6b
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class benchmarking{

    public static class ProfileResult{
        public String Name;
        long Start, End;
        ProfileResult(String name, long start, long end){
            Name = name;
            Start = start;
            End = end;
        }

    }

    public static class Instrumentor{
        String m_CurrentSession;
        FileOutputStream m_OutputStream;
        File m_File;
        int m_profileCount;
        private static Instrumentor instance = new Instrumentor();
        public Instrumentor(){
            m_profileCount = 0;
            m_OutputStream = null;
        }

        public void BeginSession(String name, String filepath) throws FileNotFoundException, IOException{
            m_CurrentSession = name;
            m_File = new File(filepath);
            m_OutputStream = new FileOutputStream(m_File);
            WriteHeader();
        }
        public void EndSession() throws IOException{
            WriteFooter();
            m_OutputStream.close();
            m_profileCount = 0;
            m_CurrentSession = null;
        }

        public void WriteProfile(ProfileResult result) throws IOException{
            
            String content = ",";
            if(m_profileCount++ >0) m_OutputStream.write(content.getBytes());

            String name = result.Name;
            content =   "{";
            content +=  "\"cat\":\"function\",";
            content +=  "\"dur\":" + (result.End - result.Start) + ',';
            content +=  "\"name\":\"" + name + "\",";
            content +=  "\"ph\":\"X\",";
            content +=  "\"pid\":0,";
            content +=  "\"tid\":0,";
            content +=  "\"ts\":" + result.Start;
            content +=  "}";
            m_OutputStream.write(content.getBytes());
            m_OutputStream.flush();

        }

        public static Instrumentor GetInstance(){
            return instance;

        }


        void WriteHeader() throws IOException{
            String content = "{\"otherData\": {},\"traceEvents\":[";
            m_OutputStream.write(content.getBytes());
            m_OutputStream.flush();
        }

        void WriteFooter() throws IOException{
            String content = "]}";
            m_OutputStream.write(content.getBytes());
            m_OutputStream.flush();
        }
    }


    public static class InstrumentationTimer{
        private static String m_Name;
        private long m_StartTimepoint;
        private boolean m_Stopped;

        public InstrumentationTimer(){}
        public InstrumentationTimer(String name)
        {
            m_Name = name; 
            m_StartTimepoint = System.nanoTime();
            m_Stopped = false;
        }

        void End() throws IOException{
            if(!m_Stopped) Stop();
        }

        public void Stop() throws IOException{
            long endTimepoint = System.nanoTime();
            Instrumentor.GetInstance().WriteProfile(new ProfileResult(m_Name,m_StartTimepoint, endTimepoint));
            m_Stopped = true;
        }

        
    }

    public static void Test() throws IOException{
        InstrumentationTimer test = new InstrumentationTimer("test");
        for(int i =0; i < 50; i++){
            int k = i;
        }
        test.End();
    }

    public static void Test2() throws IOException{
        InstrumentationTimer test = new InstrumentationTimer("test2");
        for(int i =0; i < 50; i++){
            int k = i;
        }
        test.End();
    }




    public static void main(String[] args)  throws IOException{
        Instrumentor.GetInstance().BeginSession("test", "out.json");
        Test();
        Test2();
        Instrumentor.GetInstance().EndSession();
    }

}

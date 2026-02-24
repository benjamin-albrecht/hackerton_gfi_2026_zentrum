import { Routes, Route, Navigate } from "react-router-dom";
import Layout from "./components/Layout";
import UploadPage from "./components/UploadPage";
import ExtractionList from "./components/ExtractionList";
import ExtractionDetail from "./components/ExtractionDetail";

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<ExtractionList />} />
        <Route path="/upload" element={<UploadPage />} />
        <Route path="/extractions/:id" element={<ExtractionDetail />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}
